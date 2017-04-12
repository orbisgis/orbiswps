package org.orbiswps.server

import org.orbiswps.groovyapi.input.JDBCColumnInput
import org.orbiswps.groovyapi.input.JDBCTableInput
import org.orbiswps.groovyapi.input.JDBCValueInput
import org.orbiswps.groovyapi.output.JDBCColumnOutput
import org.orbiswps.groovyapi.output.JDBCTableOutput
import org.orbiswps.groovyapi.output.JDBCValueOutput
import org.orbiswps.groovyapi.process.Process

/********************/
/** Process method **/
/********************/

/**
 * Test script for the JDBCValue
 * @author Sylvain PALOMINOS
 * @author Erwan Bocher
 */
@Process(title = ["JDBCValue test","en","Test du JDBCValue","fr"],
        description = ["Test script using the JDBCValue ComplexData.","en",
                "Scripts test pour l'usage du ComplexData JDBCValue.","fr"],
        keywords = ["test,script,wps","en","test,scripte,wps","fr"],
        identifier = "orbisgis:test:jdbcvalue",
        metadata = ["website","metadata"]
)
def processing() {
    jdbcValueOutput = inputValue;
}


/****************/
/** INPUT Data **/
/****************/

@JDBCTableInput(title = "JDBCTable for the JDBCValue",
        identifier = "orbisgis:test:jdbctable:input")
String jdbcTableInput

@JDBCColumnInput(title = "JDBCColumn for the JDBCValue",
        identifier = "orbisgis:test:jdbccolumn:input",
        jdbcTableReference = "orbisgis:test:jdbctable:input")
String jdbcColumnInput

/** This JDBCValue is the input data source. */
@JDBCValueInput(
        title = ["Input JDBCValue","en","Entrée JDBCValue","fr"],
        description = ["A JDBCValue input.","en","Une entrée JDBCValue.","fr"],
        keywords = ["input","en","entrée","fr"],
        jdbcColumnReference = "orbisgis:test:jdbccolumn:input",
        minOccurs = 0,
        maxOccurs = 2,
        identifier = "orbisgis:test:jdbcvalue:input",
        metadata = ["website","metadata"]
        )
String inputJDBCValue

/*****************/
/** OUTPUT Data **/
/*****************/

@JDBCTableOutput(title = "JDBCTable for the JDBCValue",
        identifier = "orbisgis:test:jdbctable:output")
String jdbcTableOutput

@JDBCColumnOutput(title = "JDBCColumn for the JDBCValue",
        identifier = "orbisgis:test:jdbccolumn:output",
        jdbcTableReference = "orbisgis:test:jdbctable:output")
String jdbcColumnOutput

/** This JDBCValue is the output data source. */
@JDBCValueOutput(
        title = ["Output JDBCValue","en","Sortie JDBCValue","fr"],
        description = ["A JDBCValue output.","en","Une sortie JDBCValue.","fr"],
        keywords = ["output","en","sortie","fr"],
        jdbcColumnReference = "orbisgis:test:jdbccolumn:output",
        multiSelection = true,
        identifier = "orbisgis:test:jdbcvalue:output",
        metadata = ["website","metadata"]
)
String jdbcValueOutput

