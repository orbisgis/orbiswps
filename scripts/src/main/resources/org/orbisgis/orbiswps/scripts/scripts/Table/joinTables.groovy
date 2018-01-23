/*
 * OrbisWPS contains a set of libraries to build a Web Processing Service (WPS)
 * compliant with the 2.0 specification.
 *
 * OrbisWPS is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 *
 * OrbisWPS is distributed under GPL 3 license.
 *
 * Copyright (C) 2015-2018 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisWPS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisWPS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisWPS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.orbiswps.scripts.scripts.Table

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/



/**
 * This process joins two tables.
 * @return A database table or a file.
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
		title = "Tables join",
		description = "SQL join between two tables.",
		keywords = ["Table","Join"],
		properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
                version = "1.0")
def processing() {

	if(createIndex!=null && createIndex==true){
		sql.execute "create index on "+ rightJDBCTable + "("+ rightField[0] +")"
		sql.execute "create index on "+ leftJDBCTable + "("+ leftField[0] +")"
	}

	String query = "CREATE TABLE "+outputTableName+" AS SELECT * FROM "

        if(operation[0].equals("left")){
        query += leftJDBCTable + "LEFT JOIN " + rightJDBCTable + " ON " + leftJDBCTable+ "."+ leftField[0]+ "="+ rightJDBCTable+"."+ rightField[0];
        }
    
        else if (operation[0].equals("left_without_b")){
        query += leftJDBCTable + "LEFT JOIN " + rightJDBCTable + " ON " + leftJDBCTable+ "."+ leftField[0]+ "="+ rightJDBCTable+"."+ rightField[0] 
        + " where " + rightJDBCTable+"."+ rightField[0] + " IS NULL";
        }
    
    
        else if (operation[0].equals("right")){
        query += leftJDBCTable + "RIGHT JOIN " + rightJDBCTable + " ON " + leftJDBCTable+ "."+ leftField[0]+ "="+ rightJDBCTable+"."+ rightField[0];
        
        }
        
        else if (operation[0].equals("right_without_a")){
        query += leftJDBCTable + "RIGHT JOIN " + rightJDBCTable + " ON " + leftJDBCTable+ "."+ leftField[0]+ "="+ rightJDBCTable+"."+ rightField[0] 
        + " where " + leftJDBCTable+ "."+ leftField[0] + " IS NULL";
        }
        
        else if (operation[0].equals("inner")){
            query += leftJDBCTable + "INNER JOIN " + rightJDBCTable + " ON " + leftJDBCTable+ "."+ leftField[0]+ "="+ rightJDBCTable+"."+ rightField[0];
	}
        
        else if (operation[0].equals("cross")){
            query += leftJDBCTable + "CROSS JOIN " + rightJDBCTable ;	
	}
        
        else if (operation[0].equals("natural")){
            query += leftJDBCTable + "NATURAL JOIN " + rightJDBCTable ;	
	}
        
	//Execute the query
	sql.execute(query);
        
        if(dropInputTables){
            sql.execute "drop table if exists " + leftJDBCTable+","+ rightJDBCTable
        }
	literalOutput = i18n.tr("Process done")
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the left model source. */
@JDBCTableInput(
		title = "Left table",
		description = "The left table used for the join.")
String leftJDBCTable

/** This JDBCTable is the right model source. */
@JDBCTableInput(
		title = "Right table",
		description = "The right table  used for the join.")
String rightJDBCTable

/**********************/
/** INPUT Parameters **/
/**********************/

/** Name of the identifier field of the left jdbcTable. */
@JDBCColumnInput(
		title = "Left column identifier",
		description = "The column name identifier of the left table.",
        jdbcTableReference = "leftJDBCTable",
        excludedTypes = ["GEOMETRY"])
String[] leftField

/** Name of the identifier field of the right jdbcTable. */
@JDBCColumnInput(
		title = "Right column identifier",
		description = "The column name identifier of the right table.",
        jdbcTableReference = "rightJDBCTable",
        excludedTypes = ["GEOMETRY"])
String[] rightField


@EnumerationInput(
		title = "Operation",
		description = "Types of join.",
        values=["left","right","left_without_b", "right_without_a", "inner", "cross","natural"],
        names=["Left join","Right join","Left join without rigth values","Right join without left values","Inner join","Cross join","Natural join"],
        multiSelection = false)
String[] operation = ["left"]


@LiteralDataInput(
		title = "Create indexes",
		description = "Create an index on each field identifiers to perform the join.",
		minOccurs = 0)
Boolean createIndex

@LiteralDataInput(
    title = "Drop the input tables",
    description = "Drop the input tables when the script is finished.")
Boolean dropInputTables 

@LiteralDataInput(
		title = "Output table name",
		description = "Name of the table containing the result of the process.")
String outputTableName

/*****************/
/** OUTPUT Data **/
/*****************/

/** String output of the process. */
@LiteralDataOutput(
		title = "Output message",
		description = "The output message.")
String literalOutput

