package nl.klpd.tde.treegraphwalker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * A half instantiated version of the TreegraphWalker that processes files.  
 * @author joep
 *
 * @param <NodeStateType>
 * @param <Ex>
 */
public class FileTreegraphWalker<NodeStateType, Ex extends Throwable> extends TreegraphWalker<File, NodeStateType, Ex> {



	@Override
	/**
	 * Retrieves the subnodes of a file e.g. if the file is a directory, then the files in the directory are
	 * retrieved. 
	 */
	protected Iterator<File> getSubNodeIterator(File inNode) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<File> subNodes = new ArrayList<File>();
		if (inNode.isDirectory()){
			
			for(File dirEntry: inNode.listFiles()){
			
				subNodes.add(dirEntry);
			}
			
		}
		return subNodes.iterator();		
	}

}
