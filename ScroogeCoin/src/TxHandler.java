import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

	private UTXOPool utxoPool;
	
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public boolean isValidTx(Transaction tx) {
    	
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
    	
    	//Condition 1
       	for (int i=0; i<inputs.size(); i++) {
       		Transaction.Input in = tx.getInput(i);
    		UTXO utxo = new UTXO (in.prevTxHash, in.outputIndex);
    		if (!this.utxoPool.contains(utxo)) {
    			return false;
    		}
       	}	
       	
    	//Condition 2
       	for (int i=0; i<inputs.size(); i++) {
       		Transaction.Input in = tx.getInput(i);
    		UTXO utxo = new UTXO (in.prevTxHash, in.outputIndex);
    		Transaction.Output prevOutTx = this.utxoPool.getTxOutput(utxo);
   			if (prevOutTx==null) return false;
    		boolean isOk = Crypto.verifySignature(prevOutTx.address, tx.getRawDataToSign(i), in.signature);
			if (!isOk) {
				return false;
			}
    	}

       	//Condition 3
       	Set<UTXO> setUTXO = new HashSet<>();
       	for (int i=0; i<inputs.size(); i++) {
       		Transaction.Input in = tx.getInput(i);
    		UTXO utxo = new UTXO (in.prevTxHash, in.outputIndex);
    		if (setUTXO.contains(utxo)) {
    			return false;
    		}
    		else {
    			setUTXO.add(utxo);
    		}
       	}	
       	

       	//Condition 4   	
    	ArrayList<Transaction.Output> outputs = tx.getOutputs();    	
    	for (Transaction.Output out: outputs) {
    		if (out.value < 0) {
    			return false;
    		}
    	}
    	
    	
    	//Condition 5
    	double fee = calculateTxFee(tx);
    	
    	if (fee < 0) {
    		return false;
    	}
    	
    	return true;
    }

	private double calculateTxFee(Transaction tx) {
		double inputTotal = 0;
    	for (int i=0; i<tx.getInputs().size(); i++) {
       		Transaction.Input in = tx.getInput(i);
    		UTXO utxo = new UTXO (in.prevTxHash, in.outputIndex);
    		Transaction.Output prevOutTx = this.utxoPool.getTxOutput(utxo);
    		inputTotal += prevOutTx.value;
    	}
    	
    	double outputTotal = 0;
    	for (Transaction.Output out: tx.getOutputs()) {
    		outputTotal += out.value;
    	}
    	
    	double fee = inputTotal - outputTotal;
		return fee;
	}

    
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	List<Transaction> txInAnalysis = new ArrayList<>();
    	for (Transaction t: possibleTxs) {
    		txInAnalysis.add(t);
    	}
    			
    	List<Transaction> selectedTxs = new ArrayList<>();    	

    	int indexTx = getIndexNextValidTransaction(txInAnalysis);
    	
    	while (indexTx!=-1) {
    		
    		Transaction tx = txInAnalysis.get(indexTx);
    		txInAnalysis.remove(indexTx);
    		selectedTxs.add(tx);
        	this.updateUTXOPool(tx);        	    		
    		indexTx = getIndexNextValidTransaction(txInAnalysis);
    	      		
    	}
    	
    	Transaction[] selectedTxsInArray = new Transaction[selectedTxs.size()];
    	return selectedTxs.toArray(selectedTxsInArray);

    }
    
	private int getIndexNextValidTransaction(List<Transaction> txInAnalysis) {
		
		for (int i=0; i<txInAnalysis.size(); i++) {
			Transaction tx = txInAnalysis.get(i);
			if (isValidTx(tx)) {
				return i;
			}
		}		
		return -1;
	}
    
	private void updateUTXOPool(Transaction tx) {
		//remove used utxos
		for (int i=0; i<tx.getInputs().size(); i++) {
			Transaction.Input in = tx.getInput(i);
			UTXO utxo = new UTXO (in.prevTxHash, in.outputIndex);
			this.utxoPool.removeUTXO(utxo);
		}
		
		//add new utxos
		for (int i=0; i<tx.getOutputs().size(); i++) {
			UTXO utxo = new UTXO (tx.getHash(), i);
			this.utxoPool.addUTXO(utxo, tx.getOutput(i));
		}
		
	}

}
