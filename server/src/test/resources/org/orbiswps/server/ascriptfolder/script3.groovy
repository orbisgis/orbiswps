package org.orbiswps.server.ascriptfolder

import org.orbiswps.groovyapi.output.LiteralDataOutput
import org.orbiswps.groovyapi.process.Process

/********************/
/** Process method **/
/********************/

/**
 * Test script for the script1
 * @author Sylvain PALOMINOS
 */
@Process(title = "script3",identifier = "script3ID")
def processing() {}
@LiteralDataOutput(title = "Output")
String output

