package nl.klpd.tde.ocfa.module;

import java.util.List;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.evidence.EvidenceFactory;
import nl.klpd.tde.ocfa.evidence.Meta;
import nl.klpd.tde.ocfa.message.CastType;
import nl.klpd.tde.ocfa.message.ContentType;
import nl.klpd.tde.ocfa.message.Message;
import nl.klpd.tde.ocfa.message.MessageType;
import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.message.OutgoingMailBox;
import nl.klpd.tde.ocfa.misc.EvidenceIdentifier;
import nl.klpd.tde.ocfa.misc.OcfaException;
import nl.klpd.tde.ocfa.store.DigestPair;
import nl.klpd.tde.ocfa.store.EvidenceStoreEntity;
import nl.klpd.tde.ocfa.store.Repository;
import nl.klpd.tde.treegraphwalker.NodeProcessor;

import org.apache.log4j.Logger;

/**
 * Implementation of a node processor for use within ocfa. It on the processBefore it will generate an evidence
 * out of the Node. On the ProcessAfter it will send a message with the evidence to the router. The 
 * Evidence is kept as state.
 * @author joep
 * @codereview jochen
 *
 * @param <NodeType>
 */
public abstract class OcfaNodeProcessor<NodeType> implements NodeProcessor<NodeType, Evidence, OcfaException> {

	/**
	 * May be rename this to relationType.
	 * @author joep
	 * Values from ocfa.xsd:
	 *  value="dirdirentry" 
     *  value="filedirentry" 
     *  value="specialdirentry" 
     *  value="removedentry" 
     *  value="partitionentry" 
     *  value="swappartitionentry" 
     *  value="unallocated" 
     *  value="undefined" 
     *  value="slack"
     *  removed "image" relationtype
     *  value="lost" 
     *  value="fsinfo"
     *  value="content"
     *  value="headers"
	 *  For production systems schema check is turned off
	 */
	public enum CurrentNodeType {
		DIR("dirdirentry"), 
		FILE("filedirentry"), 
		SPECIAL("specialdirentry"),
		DELETED("removedentry"), 
		PARTITION("partitionentry"),
		SWAP("swappartitionentry"),
		UNALLOCATED("unallocated"),
		UNSPECIFIED("undefined"),
		SLACK("slack"),
		LOST("lost"), 
	    FSINFO("fsinfo"),
	    CONTENT("content"),
	    HEADERS("headers"),
	    ALTERNATE_STREAM("alternate stream")
	    ;
		
		private String relName;
		
		private CurrentNodeType(String inRelName){
			
			relName = inRelName;
		}
		
		public String getRelName(){
			
			return relName;
		}
	};

	private String parentChildRelation;
	private EvidenceFactory evidenceFactory;
	private OutgoingMailBox mailSender;
	private Repository repository;
	private ModuleInstance routerAddress;
	private ModulePostNodeProcessor<NodeType> postProcessor = null;
	private Logger log = Logger.getLogger("nl.klpd.tde.ocfa.module.OcfaNodeProcessor");
	
	public OutgoingMailBox getMailSender() {
		
		return mailSender;
	}

	public void setMailSender(OutgoingMailBox mailSender) {
		 this.mailSender = mailSender;
	}

	public EvidenceFactory getEvidenceFactory() {
		return evidenceFactory;
	}

	public void setEvidenceFactory(EvidenceFactory evidenceFactory) {
		this.evidenceFactory = evidenceFactory;
	}

	/**
	 * sends the evidence to the router and inserts it into the database as a metahandle.
	 * @param inNode The node for which the evidence was made.
	 * @param inEvidence theEvidence that should be tested.
	 */
	public Evidence processAfter(NodeType inNode, Evidence inEvidence) throws OcfaException {
		//Hook to attach a extra postProcessor from module code
		//Should be solved with a listener
		if (postProcessor != null){
			postProcessor.postProcess(inNode, inEvidence);
		}
		
		inEvidence.getActiveJob().close();
		String evidenceString = evidenceFactory.evidenceAsString(inEvidence);
		EvidenceIdentifier identifier= new EvidenceIdentifier();
		identifier.setCaseName(inEvidence.getCase());
		identifier.setEvidenceSourceID(inEvidence.getSource());
		identifier.setItemName(inEvidence.getItem());
		identifier.setEvidenceID(inEvidence.getEvidenceId());
		String metaHandle = repository.insertMeta(evidenceString, inEvidence.getDataHandle(), identifier);
		
		Message message = mailSender.getMessageFactory().createMessage();
		message.setContent(metaHandle);
		message.setCastType(CastType.ANYCAST);
		message.setReceiver(routerAddress);
		message.setPrio(6);
		message.setContentType(ContentType.EVIDENCE);
		message.setMessageType(MessageType.USER);
		message.setSubject("newevidence");
		mailSender.sendMessage(message);
		return inEvidence;
	}

	/**
	 * processes a node, by creating an evidence from it. Also creates a evidencestoreentity for
	 * inNode.
	 */
	public Evidence processBefore(NodeType inNode, Evidence inParentEvidence) throws OcfaException {
		
		List<Meta> metaData = getMetaDataFromNode(inNode);
		EvidenceStoreEntity evStoreEntity= getEvidenceStoreEntity(inNode);
		String dataHandle = null;
		DigestPair pair = null;
		if (evStoreEntity != null){
			
			dataHandle = evStoreEntity.getHandle();
			pair = evStoreEntity.getPair();
		}
		
		/* Without dataHandle and pair, it could for example be a directory node, 
		so go on filling a newEvidence */
		
		String evidenceName = getEvidenceName(inNode);
		
		Evidence newEvidence = evidenceFactory.createEvidence(dataHandle, pair, evidenceName, 
				inParentEvidence, getParentChildRelation());
		log.info("new Evidence is" + evidenceFactory.evidenceAsString(newEvidence));
		for(Meta meta : metaData){
			
			newEvidence.getActiveJob().setMeta(meta.getName(), meta.getValue(), meta.getType());
		}
		
		return newEvidence;
	}

	/**
	 * Abstract method that retrieves the evidence name from a node.
	 * @param inNode
	 * @return a name describing this evidence, e.g., the filename if the node is a file.
	 * @throws OcfaException 
	 */
	abstract public String getEvidenceName(NodeType inNode) throws OcfaException;

	/**
	 * Abstract method that creates hte evidencestoreentity from a node.
	 * @param inNode
	 * @return
	 * @throws OcfaException
	 */
	abstract public EvidenceStoreEntity getEvidenceStoreEntity(NodeType inNode) throws OcfaException;

	/**
	 * Abstract method that retrieves a list of metadata form a node. This is then added to 
	 * the evidence.
	 * @param inNode
	 * @return
	 */
	abstract public List<Meta> getMetaDataFromNode(NodeType inNode);
	
	public String getParentChildRelation() {
		return parentChildRelation;
	}

	public void setParentChildRelation(String parentChildRelation) {
		this.parentChildRelation = parentChildRelation;
	}

	public ModuleInstance getRouterAddress() {
		return routerAddress;
	}

	public void setRouterAddress(ModuleInstance routerAddress) {
		this.routerAddress = routerAddress;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public ModulePostNodeProcessor<NodeType> getPostProcessor() {
		return postProcessor;
	}

	public void setPostProcessor(ModulePostNodeProcessor<NodeType> postProcessor) {
		this.postProcessor = postProcessor;
	}

	
	

}
