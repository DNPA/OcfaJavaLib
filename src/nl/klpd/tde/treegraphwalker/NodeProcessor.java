package nl.klpd.tde.treegraphwalker;

/**
 * Interface for an object that processes different nodes.
 * @author joep
 * @codereview jochen
 * @see TreegraphWalker
 * @param <NodeType> the kind of node that should be processed.
 * @param <NodeStateType> object that holds state.
 * @param <Ex>
 */
public interface NodeProcessor<NodeType, NodeStateType, Ex extends Throwable> {

		/**
		 * processBefore. processing that takes place before subnodes are processed.
		 * @param inNode the node that should be processed.
		 * @param inParentState a state that is initialized by the pareend.
		 * @return a new state describing the processing of the current node.
		 * @throws Ex
		 */
		NodeStateType processBefore(NodeType inNode, NodeStateType inParentState) throws Ex;
		
		/**
		 * processAfter processing that takes place after subnodes are processed.
		 * @param inNode the node that should
		 * @param ownState the state that was delivered by processBefore.
		 * @return ownstate
		 * @throws Ex
		 */
		NodeStateType processAfter(NodeType inNode, NodeStateType ownState) throws Ex;
}
