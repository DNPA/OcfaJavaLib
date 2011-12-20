package nl.klpd.tde.ocfa.module;

import nl.klpd.tde.ocfa.evidence.Evidence;

/**
 * Hook to create custom adding of metaname etc. 
 * @author joep
 *
 * @param <NodeType>
 */
public interface ModulePostNodeProcessor<NodeType> {

	public void postProcess(NodeType inNode, Evidence ioEvidence);
}
