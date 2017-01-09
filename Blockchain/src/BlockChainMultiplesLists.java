import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.


/* This will not work because it will be complicated to find the right parent with this
 * lists. For example, I can create many lists finding the same node in the middle of one list.
 */

public class BlockChainMultiplesLists {
	
    public static final int CUT_OFF_AGE = 15;
    public static final int MAX_OLD_AGE = 200;
    
    private long ageGenerator = 0;

    
    //Transacoes disponiveis para formar um bloco
    private TransactionPool txPool;

    
    class BlockDecorator {
    	public BlockDecorator(Block block, long age) {
			super();
			this.block = block;
			this.age = age;
		}
		Block block;
    	long age;
    	
    }
    
    //estrutura para manter os blocos
    /*
     - cada bloco precisa ter sua altura ou ser deduzida
     - deve referenciar outro bloco que esteja em uma altura anterior
     
     Maintain only one global Transaction Pool for the block chain and keep adding transactions to
it on receiving transactions and remove transactions from it if a new block is received or
created. It is s okay if some transactions get dropped during a block chain reorganization, i.e.,
when a side branch becomes the new longest branch. Specifically, transactions present in the
original main branch (and thus removed from the transaction pool) but absent in the side
branch might get lost.
     
     Ao inves de manter uma arvore, acho que eh melhor manter uma lista de fins do blockchain.
     Posso tb manter uma lista de utxo que bate 1-a-1 com a lista de fins do blockchain.
     
      */
    
    
    
    private List<List<BlockDecorator>> blocksBackwardList;
    private List<UTXOPool> utxoPoolForEachBlocksBackwardList;
    
    
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChainMultiplesLists(Block genesisBlock) {
        
    	this.txPool = new TransactionPool();
        
        this.blocksBackwardList = new ArrayList<>();
        this.utxoPoolForEachBlocksBackwardList = new ArrayList<>();
        
        
        //create the first list of blockchain
        List<BlockDecorator> firstPossibleBlocksListBackwards = new LinkedList<>();
        firstPossibleBlocksListBackwards.add(new BlockDecorator(genesisBlock, 0));
        this.blocksBackwardList.add(firstPossibleBlocksListBackwards);
                
        UTXOPool utxoPool = new UTXOPool();
        
        UTXO utxo = new UTXO(genesisBlock.getCoinbase().getHash(), 0);
        utxoPool.addUTXO(utxo, genesisBlock.getCoinbase().getOutput(0));
        
        ArrayList<Transaction> blockTxs = genesisBlock.getTransactions();
        
        for (Transaction tx : blockTxs) {
        	
        	//For each output of tx
        	for (int i=0; i<tx.getOutputs().size(); i++) {
                utxo = new UTXO(tx.getHash(), i);
            	utxoPool.addUTXO(utxo, tx.getOutput(i));        		
        	}
        }
        
        this.utxoPoolForEachBlocksBackwardList.add(utxoPool);
        
    }

    
    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    	
    	/* If there are multiple blocks at the same height, return the oldest block in
getMaxHeightBlock() function. ---> smaller index
    	 */
    	int indexMax = getIndexOfMaxHeight();
    	return this.blocksBackwardList.get(indexMax).get(0).block;
    }


	private int getIndexOfMaxHeight() {

		int indexOfMaxHeihtBlock = 0;
    	int maxHeight = 1;
    	
    	for (int i=0; i<this.blocksBackwardList.size(); i++) {
    		List<BlockDecorator> blocksBackward = this.blocksBackwardList.get(i);

    		if (blocksBackward.size()>maxHeight) {
    			indexOfMaxHeihtBlock=i;
    			maxHeight=blocksBackward.size();
    		}
    	}
    	return indexOfMaxHeihtBlock;
	}
     
    
    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
    	int indexMax = getIndexOfMaxHeight();
    	return this.utxoPoolForEachBlocksBackwardList.get(indexMax);
    }

    
    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
    	return new TransactionPool(txPool);
    }

    /**
     * Add {@code block} to the block chain if it is valid. 
     * For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {

    	byte[] prevBlockHash = block.getPrevBlockHash();
    	
    	
    /*	A new genesis block will not be mined. If you receive a block which claims to be a genesis block
    	(parent is a null hash) in the addBlock(Block b) function, you can return false . */
    	if (prevBlockHash==null) {
    		return false;
    	}

    	
    	/*
    	 * Find where this block will be located 
    	 */
		ByteArrayWrapper baPrevBlockHash = new ByteArrayWrapper(prevBlockHash);    	
    	boolean blockAdded = false;
    	
    	for (int i=0; i<this.blocksBackwardList.size() && !blockAdded; i++) {
    		List<BlockDecorator> oneList = this.blocksBackwardList.get(i);
    		
    		
    		for (int j=0; j<oneList.size() && !blockAdded; j++) {
    			
    			BlockDecorator oneBlock = oneList.get(j);
    			ByteArrayWrapper oneBlockHash = new ByteArrayWrapper(oneBlock.block.getHash());
    			
    			if (baPrevBlockHash.equals(oneBlockHash)) {
    				
    				UTXOPool updatedUtxo = verifyValidTxSetAndDoubleSpend(i, block);
    				
    				if (updatedUtxo!=null) {
        				    					
        				if (j==0){//era o primeiro elemento da lista

        					oneList.add(0, new BlockDecorator(block, ageGenerator));
        					
        					
        					if (oneList.size()>CUT_OFF_AGE) {
        						oneList.remove(CUT_OFF_AGE);
        					}
        					
        		        	utxoPoolForEachBlocksBackwardList.set(i, updatedUtxo);        	

        				}
        				
        				else {//era um elemento intermediario

        					//cria uma nova lista
        					List<BlockDecorator> newList = new LinkedList<>();
        					newList.add(new BlockDecorator (block, ageGenerator));
        					for (; j<oneList.size(); j++) {
        						newList.add(oneList.get(j));
        					}
        					
        					this.blocksBackwardList.add(newList);
        		        	utxoPoolForEachBlocksBackwardList.add(updatedUtxo);        	

        		        	
            	    		//removes old lists based on the first element age
            	    		for (int z=0; z<this.blocksBackwardList.size(); z++) {
            	    			if (this.ageGenerator - this.blocksBackwardList.get(z).get(0).age > MAX_OLD_AGE ) {
            	    				this.blocksBackwardList.remove(z);
            	    			}
            	    		}

        				}

        	        	
        	        	this.ageGenerator++;
        	        	
        	        	//update tx pool
        	        	for (Transaction tx: block.getTransactions()) {
        	                this.txPool.removeTransaction(tx.getHash());         		
        	        	}
        				
    				} 
    				
    			}
    		}
    	}
         	
    	
    	return blockAdded;
    }

    private UTXOPool verifyValidTxSetAndDoubleSpend(int i, Block block) {
    	
    	UTXOPool utxoPoolOriginal = this.utxoPoolForEachBlocksBackwardList.get(i);
    	UTXOPool utxoPoolToTest = new UTXOPool(utxoPoolOriginal);    	    	    	       			

    	
    	//verify double spend
      	for (Transaction tx: block.getTransactions()) {

      		for (int j=0; j< tx.getInputs().size(); j++) {
    			Transaction.Input input = tx.getInput(j);
    		    UTXO utxo = new UTXO (input.prevTxHash, input.outputIndex);
    		    
    		    if (!utxoPoolToTest.contains(utxo)) {
    		    	return null;
    		    }
    		    else {
    		    	utxoPoolToTest.removeUTXO(utxo);
    		    }
    	
    		} 
    	}
    	
    	    	
    	return verifyValidTxSet(i, block); 
	}


	private UTXOPool verifyValidTxSet(int i, Block block) {
		
		
		//Need UTXOPool of the correct parent to build TxHandler
		TxHandler txHandler = new TxHandler(utxoPoolForEachBlocksBackwardList.get(i));
		
    	/*
    	 * When checking for validity of a newly received block, 
    	 * just checking if the transactions form a valid set is enough.
    	 */   	    	
    	List<Transaction> blockTxs = new ArrayList<>(block.getTransactions());
    	Transaction bTx[] = blockTxs.toArray(new Transaction[0]);

    	Transaction returnedTx[] = txHandler.handleTxs(bTx);
    	    	
        if (bTx.length != returnedTx.length) {
           return null;
		}		
        
        
        //add coinbase
        UTXOPool utxoPool = txHandler.getUTXOPool();
        Transaction coinbase = block.getCoinbase();
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
        utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));

        
		return utxoPool;
	}


	/** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
    	
        this.txPool.addTransaction(tx);
    }
    
}