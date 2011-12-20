package nl.klpd.tde.ocfa.module;
import java.io.File;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.treegraphwalker.FileTreegraphWalker;
import nl.klpd.tde.treegraphwalker.TreegraphWalker;
/**
 * Extension on OcfaDissector that allow a filetreewalker to walk over derived evidences in the working 
 * directory and sends them to the 
 * @author joep
 *
 */
public class OcfaFileDissector extends OcfaDissector {

	public OcfaFileDissector(String inName, String inNameSpace)
			throws OcfaException {
		super(inName, inNameSpace);
		setTreeWalker(new FileTreegraphWalker<Evidence, OcfaException>());
		OcfaFileNodeProcessor processor = new OcfaFileNodeProcessor();
		processor.setEvidenceFactory(this.getFactory());
		processor.setMailSender(this.getConnector().getMailSender());
		processor.setParentChildRelation("direntry");
		ModuleInstance router = new ModuleInstance();
		router.setNamespace("core");
		router.setModuleName("router");
		processor.setRouterAddress(router);
		processor.setRepository(this.getRepository());
		getTreeWalker().setNodeProcessor(processor);
		//getRepository();
	}

	protected TreegraphWalker<File, Evidence, OcfaException> treeWalker;

	
	
	public TreegraphWalker<File, Evidence, OcfaException> getTreeWalker() {
		return treeWalker;
	}

	public void setTreeWalker(
			TreegraphWalker<File, Evidence, OcfaException> treeWalker) {
		this.treeWalker = treeWalker;
	}
	
	
//	this.getConnector().getOwnAddress().getModuleName()
}
