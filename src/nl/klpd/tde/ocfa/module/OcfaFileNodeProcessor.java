package nl.klpd.tde.ocfa.module;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.klpd.tde.ocfa.evidence.Meta;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.store.EvidenceStoreEntity;

/**
 * OcfaNodeProcessor for use with files.
 * Travels a directory tree and sends every file in it to the router.
 * @author joep
 * @codereview jochen
 */
public class OcfaFileNodeProcessor extends OcfaNodeProcessor<File> {

	
//	private boolean shouldDelete = false;
	@Override
	public EvidenceStoreEntity getEvidenceStoreEntity(File inNode) throws OcfaException {
		// TODO Auto-generated method stub
		if (inNode.isFile()){
			return getRepository().createEvidenceStoreEntity(inNode);
		} else {
			
			return null;
		}
	}

	@Override
	/** 
	 * gets the list of metadatafrom a file. For now it is only the fact 
	 * that it is a file or a diredctory
	 * 
	 */
	public List<Meta> getMetaDataFromNode(File inNode) {

		List<Meta> metaList = new ArrayList<Meta>();
		try {
		if (inNode.isDirectory()){
			
			this.setParentChildRelation(OcfaNodeProcessor.CurrentNodeType.DIR.getRelName());
			metaList.add(new Meta("inodetype", "dir"));
			metaList.add(new Meta("nodetype", "dir"));

		}
		else {
			
			this.setParentChildRelation(OcfaNodeProcessor.CurrentNodeType.FILE.getRelName());
			metaList.add(new Meta("inodetype", "file"));
			metaList.add(new Meta("nodetype", "file"));

			metaList.add(new Meta("size", Long.toString(inNode.length())));
		}
		} catch(OcfaException e){
		
			try {
				metaList.add(new Meta("error", "error adding metadata:" + e.getMessage()));
			} catch (OcfaException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		
		}
		return metaList;
	}

	@Override
	public String getEvidenceName(File inNode) throws OcfaException {
		// TODO Auto-generated method stub
		return inNode.getName();
	}

}
