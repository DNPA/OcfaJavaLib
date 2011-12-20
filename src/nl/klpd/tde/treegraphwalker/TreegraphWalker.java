package nl.klpd.tde.treegraphwalker;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * Abstract generic class that implements walking over a tree. It is used to allow walking over a directory 
 * or a object tree as  created by e.g. snorkel.
 * 
 * 
 * 
 * @author joep
 *
 * @param <NodeType>
 * The nodes that makeup the tree.
 * @param <NodeStateType> the internal state that is kept between the nodes.
 * @param <Ex> the exceptions that can be thrown while traversing the tree.
 */
public abstract class TreegraphWalker<NodeType, NodeStateType, Ex extends Throwable> {

	Logger log = Logger.getLogger(this.getClass());
	/**
	 * The object whose methods are called upon the specific node. 
	 */
	private NodeProcessor<NodeType, NodeStateType, Ex> nodeProcessor;
	
	public NodeProcessor<NodeType, NodeStateType, Ex> getNodeProcessor() {
		return nodeProcessor;
	}

	public void setNodeProcessor(NodeProcessor<NodeType, NodeStateType, Ex> nodeProcessor) {
		this.nodeProcessor = nodeProcessor;
	}

	/**
	 * Lets the treewalker process one node. Mode is as follows:
	 * <ul>
	 * <li> Call processor.processBefore
	 * <li> Process subnodes.
	 * <li> call processor.processAfter
	 * @param inNode
	 * @param inParentState
	 * @throws Ex
	 */
	public void processNode(NodeType inNode, NodeStateType inParentState) throws Ex  {
		
		NodeStateType newState = nodeProcessor.processBefore(inNode, inParentState);
		try {
			Iterator<NodeType> iterator = getSubNodeIterator(inNode);
			while(iterator.hasNext() ){
				NodeType node = iterator.next(); 
				processNode(node, newState);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
						
			for (StackTraceElement elem : e.getStackTrace()){
					
				log.warn(elem.toString());
			}
			
			e.printStackTrace();
			
		}
		nodeProcessor.processAfter(inNode, newState);
	}

	/**
	 * Returns a iterator over all subnodes of a node. 
	 * @param inNode the node from which the subnodes should be retrieved. 
	 * @return an iterator over all subnodes.
	 * @throws Exception
	 */
	abstract protected Iterator<NodeType> getSubNodeIterator(NodeType inNode) throws Exception;
}
