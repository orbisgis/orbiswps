package org.orbisgis.orbiswps.service.process;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test class perform tests on the ProgressMonitor class
 *
 * @author Petter Teigen
 */
public class ProgressMonitorTest {
    @Test
    public void testendOfProgressSetsProgressTo1WithOneSubprocesses(){
        ProgressMonitor sometask = new ProgressMonitor("Sometask");
        sometask.endOfProgress();
        Assert.assertEquals("Expecting progress to be 1d with just one sub process", 1d, sometask.getProgression(), 0);
    }

    @Test
    public void testendOfProgressSetsProgressTo1WithSeveralSubprocesses(){
        ProgressMonitor sometask = new ProgressMonitor("Sometask");
        sometask.subProcess(10);
        sometask.endOfProgress();
        Assert.assertEquals("Expecting progress to be 1d with several sub processes", 1d, sometask.getProgression(), 0);
    }

    @Test
    public void testSeveralSubprocessesWillUpdateProgressWithEveryStep(){
        final int numSubProcesses = 20;
        ProgressMonitor sometask = new ProgressMonitor("Sometask");
        sometask.subProcess(numSubProcesses);

        for (int i = 0; i < numSubProcesses; i++) {
            Assert.assertEquals(i * 1d / numSubProcesses, sometask.getProgression(), 0);
            sometask.endStep();
        }
        Assert.assertEquals("Expecting progress to be 1d even before endOfProgress is called", 1d, sometask.getProgression(), 0);

        sometask.endOfProgress();
        Assert.assertEquals("Expecting progress to be 1d with several sub ended and endOfProgress() called", 1d, sometask.getProgression(), 0);
    }
}
