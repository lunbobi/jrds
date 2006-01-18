/*
 * Created on 7 janv. 2005
 *
 * TODO 
 */
package jrds.probe.munins;

import jrds.ProbeDesc;
import jrds.RdsHost;
import jrds.graphe.PagingSolarisMuninsGraph;

import org.apache.log4j.Logger;


/**
 * @author bacchell
 *
 * TODO 
 */
public class PagingSolarisMunins extends MuninsProbe {
	static private final Logger logger = Logger.getLogger(PagingSolarisMunins.class);

	static final private ProbeDesc pd = new ProbeDesc(1);
	static {
		pd.add("pgin", ProbeDesc.COUNTER, "pgin.value");
		pd.add("reclaim", ProbeDesc.COUNTER, "reclaim.value");
		pd.add("pgpgin", ProbeDesc.COUNTER, "pgpgin.value");
		pd.add("pgout", ProbeDesc.COUNTER, "pgout.value");
		pd.add("scan", ProbeDesc.COUNTER, "scan.value");
		pd.add("pgpgout", ProbeDesc.COUNTER, "pgpgout.value");
		pd.add("pgfree", ProbeDesc.COUNTER, "pgfree.value");
		pd.setGraphClasses(new Class[] {PagingSolarisMuninsGraph.class});
		pd.setMuninsProbesNames(new String[] { "paging_in", "paging_out" });
		pd.setName("pageingmunins");
	}

	/**
	 * @param monitoredHost
	 */
	public PagingSolarisMunins(RdsHost monitoredHost) {
		super(monitoredHost, pd);
	}
}
