import java.util.HashSet;
import java.util.Set;

public class MaliciousNode implements Node {

	
	Set<Transaction> pendingTransactions;
	
    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions  =pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
//        return new HashSet<Transaction>();
    	Set<Transaction> s = new HashSet<>();
    	if (pendingTransactions.size()>0) 	s.add((Transaction)pendingTransactions.toArray()[0]);
    	else s = new HashSet<Transaction>();
    	return s;
        
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        return;
    }
}
