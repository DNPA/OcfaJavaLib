package nl.klpd.tde.ocfa.module;

import java.io.File;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.store.DigestPair;
import nl.klpd.tde.ocfa.store.EvidenceStoreEntity;
import nl.klpd.tde.ocfa.store.OcfaRepository;
import nl.klpd.tde.treegraphwalker.FileTreegraphWalker;
import nl.klpd.tde.treegraphwalker.TreegraphWalker;
/**
 * Java version of the kickstart module.
 * @author joep
 *
 */
public class OcfaKickstarter extends OcfaModule {

	protected TreegraphWalker<File, Evidence, OcfaException> treeWalker;
	protected OcfaFileNodeProcessor processor;
	
	
	public TreegraphWalker<File, Evidence, OcfaException> getTreeWalker() {
		return treeWalker;
	}

	public void setTreeWalker(
			TreegraphWalker<File, Evidence, OcfaException> treeWalker) {
		this.treeWalker = treeWalker;
	}
	/**
	 * Constructor initializes all members.
	 * @param inName name of the module.
	 * @param inNameSpace name space in which the module operates.
	 * @throws OcfaException
	 */
	public OcfaKickstarter(String inName, String inNameSpace)
			throws OcfaException {
		super(inName, inNameSpace);
		if (getRepository() instanceof OcfaRepository){
			
			// Always copy into the repository. This is a simplification of the c++ version 
			// that can aslo simplink.
			((OcfaRepository) getRepository()).setMode(OcfaRepository.RepositoryMode.COPY);
		}
		setTreeWalker(new FileTreegraphWalker<Evidence, OcfaException>());
		processor = new OcfaFileNodeProcessor();
		processor.setEvidenceFactory(this.getFactory());
		processor.setMailSender(this.getConnector().getMailSender());
		processor.setParentChildRelation(OcfaNodeProcessor.CurrentNodeType.DIR.getRelName());
		ModuleInstance router = new ModuleInstance();
		router.setNamespace("core");
		router.setModuleName("router");
		processor.setRouterAddress(router);
		processor.setRepository(getRepository());
		getTreeWalker().setNodeProcessor(processor);

	}
	/**
	 * Kickstarts a files. 
	 * @param inCase
	 * @param inSource
	 * @param inItem
	 * @param inFile
	 * @throws OcfaException
	 */
	public void kickstartFile(String inCase, String inSource, String inItem, File inFile) throws OcfaException {
		
		getRepository().createItem(inCase, inSource, inItem);
		String dataHandle = null;
		DigestPair pair = null;
		if (inFile.isFile()){
			EvidenceStoreEntity entity = processor.getEvidenceStoreEntity(inFile);
			dataHandle = entity.getHandle();
			pair = entity.getPair();
		}
		Evidence parentEvidence = getFactory().createEvidence(dataHandle, pair, 
				"0", inCase, inSource, inItem);
		if (inFile.isDirectory()){
			
			parentEvidence.getActiveJob().setMeta("inodetype", "dir");
			for (File file : inFile.listFiles()){
			
				treeWalker.processNode(file, parentEvidence);
			}
		} else {
			
			parentEvidence.getActiveJob().setMeta("inodetype", "file");
		}
		processor.processAfter(inFile, parentEvidence);
	}
	
}
