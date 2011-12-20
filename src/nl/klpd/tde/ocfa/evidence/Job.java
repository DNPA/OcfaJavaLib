package nl.klpd.tde.ocfa.evidence;

import java.util.Iterator;

import nl.klpd.tde.ocfa.message.ModuleInstance;
import nl.klpd.tde.ocfa.misc.OcfaException;

/**
 * Interface of a job object, a description of the passing of an evidence through a module.
 * @author joep
 *
 */
public interface Job {

		Iterator<Meta> getMetaIterator();
		public int getChildCount();
		
		public void close();

		void setMeta(String inName, String inValue) throws OcfaException;
		void setMeta(String inName, String inValue, ValueType inType) throws OcfaException;
		void addChild(String inIdentifier, String inChildName,
				String inParentChildRelation);
		public ModuleInstance getModuleInstance();
	
}
