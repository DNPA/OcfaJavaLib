package nl.klpd.tde.ocfa.module;

import java.io.File;

import nl.klpd.tde.ocfa.evidence.Evidence;
import nl.klpd.tde.ocfa.message.CastType;
import nl.klpd.tde.ocfa.message.ContentType;
import nl.klpd.tde.ocfa.message.Message;
import nl.klpd.tde.ocfa.message.MessageType;

import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.EvidenceIdentifier;
import nl.klpd.tde.ocfa.misc.OcfaException;
/**
 * Extension on the default module useful for the development of dissectors.
 * It has methods to get the workdir and to derive new evidences from old.
 * @author joep
 * @codereview jochen
 *
 */
public class OcfaDissector extends OcfaModule {
	
	private String workDir;
	/**
	 * sender is used to to submit new derived evidence.
	 */
	private ModuleInstance returnAddress;
	public OcfaDissector(String inName, String inNameSpace) throws OcfaException{
		
		super(inName, inNameSpace);
		workDir = createWorkDir();		
	}

	
	
	@Override
	protected void processEvidenceMessage(Message message) throws OcfaException {

		returnAddress = message.getSender().getModuleTypeOfInstance();
		super.processEvidenceMessage(message);
	}



	protected String createWorkDir() throws OcfaException {
		String workDirRoot = getConfig().getProperty("workdirroot");

		if (workDirRoot == null){
			
			throw new OcfaException("no workdirroot");
		}
		
		StringBuffer workDirBuf = new StringBuffer(workDirRoot);
		workDirBuf.append('/');
		workDirBuf.append(this.getConnector().getOwnAddress().getNamespace());
		workDirBuf.append('/');
		workDirBuf.append(this.getConnector().getOwnAddress().getHost());
		workDirBuf.append('/');
		workDirBuf.append(this.getConnector().getOwnAddress().getModuleName());
		workDirBuf.append('/');
		workDirBuf.append(this.getConnector().getOwnAddress().getInstance());
		String workDir = workDirBuf.toString();
		new File(workDir).mkdirs();
		return workDir;
	}

	public void sendNewMessage(Evidence inEvidence) throws OcfaException{
		inEvidence.getActiveJob().close();
		String evidenceString = getFactory().evidenceAsString(inEvidence);
		EvidenceIdentifier identifier= new EvidenceIdentifier();
		identifier.setCaseName(inEvidence.getCase());
		identifier.setEvidenceSourceID(inEvidence.getSource());
		identifier.setItemName(inEvidence.getItem());
		identifier.setEvidenceID(inEvidence.getEvidenceId());
		String metaHandle = getRepository().insertMeta(evidenceString, inEvidence.getDataHandle(), identifier);
	
		Message message = getConnector().getMailSender().getMessageFactory().createMessage();
		message.setContent(metaHandle);
		message.setCastType(CastType.ANYCAST);
		message.setReceiver(returnAddress);
		message.setPrio(6);
		message.setContentType(ContentType.EVIDENCE);
		message.setMessageType(MessageType.TASK);
		message.setSubject("newevidence");
		this.getConnector().getMailSender().sendMessage(message);
	}



	public String getWorkDir() {
		return workDir;
	}
}
