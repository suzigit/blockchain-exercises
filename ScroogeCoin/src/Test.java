import java.security.PublicKey;


public class Test {
	
	public static void main(String[] args) {

		UTXOPool utxoPool = new UTXOPool();
		
		Transaction p1 = new Transaction();
		byte hashe1[] = {1};
		p1.setHash(hashe1);
		p1.addOutput(1, new PublicKey() {
			
			@Override
			public String getFormat() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte[] getEncoded() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAlgorithm() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		UTXO utxo1 = new UTXO (hashe1, 1);
		utxoPool.addUTXO(utxo1, p1.getOutput(0));
		
		
		
		Transaction p2 = new Transaction();
		byte hashe2[] = {2};
		p2.setHash(hashe2);
		p2.addOutput(2, new PublicKey() {
			
			@Override
			public String getFormat() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte[] getEncoded() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAlgorithm() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		UTXO utxo2 = new UTXO (hashe2, 2);
		utxoPool.addUTXO(utxo2, p2.getOutput(0));


		Transaction p3 = new Transaction();
		byte hashe3[] = {11};
		p3.setHash(hashe3);
		p3.addOutput(1, new PublicKey() {
			
			@Override
			public String getFormat() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte[] getEncoded() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAlgorithm() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		UTXO utxo3 = new UTXO (hashe3, 1);
		utxoPool.addUTXO(utxo3, p3.getOutput(0));
		

		
		
		
		//CRIOU POOL
		
		
		Transaction t1 = new Transaction();
		byte h1[] = {1};
		t1.addInput(h1, 1);
		byte h2[] = {2};
		t1.addInput(h2, 2);
		
		t1.addOutput(2, new PublicKey() {
			
			@Override
			public String getFormat() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte[] getEncoded() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAlgorithm() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		byte hash[] = {100};
		t1.setHash(hash);
		
		
		

		Transaction t2 = new Transaction();
		byte h11[] = {11};
		t2.addInput(h11, 1);
		
		t2.addOutput(1, new PublicKey() {
			
			@Override
			public String getFormat() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte[] getEncoded() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getAlgorithm() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		byte hash2[] = {101};
		t2.setHash(hash2);
				
		
		Transaction[] txs = new Transaction[2];
		txs[0]=t1;
		txs[1]=t2;				
		
		
		MaxFeeTxHandler txHandler = new MaxFeeTxHandler(utxoPool);
		txHandler.handleTxs(txs);
		
		
	}
	
    
    /*
     * Precisa processar uma transacao por vez, pois a saida de uma pode validar a outra!!!
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        
    	//Choose only valid transactions
    	List<Transaction> validTransactions = new ArrayList<>();
    	for (Transaction t: possibleTxs) {
    		if (this.isValidTx(t)) {
    			validTransactions.add(t);
    		}
    	}

    	List<Transaction> selectedTxs = new ArrayList<>();
       	Set<UTXO> setUsedUTXOOfSelectedTxs = new HashSet<>();
    	for (Transaction newTx: validTransactions) {

    		Set<UTXO> setUsedUTXOFromNewTx = new HashSet<>();
    		for (Transaction.Input in: newTx.getInputs()) {
    			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
    			setUsedUTXOFromNewTx.add(utxo);
    		}
    		
    		if (this.verifyTransactionCanBeIncluded(setUsedUTXOOfSelectedTxs, setUsedUTXOFromNewTx)) {
    			selectedTxs.add(newTx);
    			setUsedUTXOOfSelectedTxs.addAll(setUsedUTXOFromNewTx);
    		}
    	}
    
    	this.updateUTXOPool(setUsedUTXOOfSelectedTxs, selectedTxs);
    	
    	Transaction[] selectedTxsInArray = new Transaction[selectedTxs.size()];
    	return selectedTxs.toArray(selectedTxsInArray);
        	
    
    }
    
	private void updateUTXOPool(Set<UTXO> setUsedUTXOOfSelectedTxs,
			List<Transaction> selectedTxs) {
		//remove used utxos
		setUsedUTXOOfSelectedTxs.forEach(utxo -> this.utxoPool.removeUTXO(utxo));
		
		//add new utxos
		for(Transaction tx : selectedTxs) {
			for (int i=0; i<tx.getOutputs().size(); i++) {
				UTXO utxo = new UTXO (tx.getHash(), i);
				this.utxoPool.addUTXO(utxo, tx.getOutput(i));
			}
		}
		
	}
    private boolean verifyTransactionCanBeIncluded(
			Set<UTXO> setUsedUTXOOfSelectedTxs, Set<UTXO> setUsedUTXOFromNewTx) {

    	for (UTXO utxo : setUsedUTXOFromNewTx) {
    		if (setUsedUTXOOfSelectedTxs.contains(utxo)) {
    			return false;
    		}
    	}
    	return true;
	}
    
    */
}
