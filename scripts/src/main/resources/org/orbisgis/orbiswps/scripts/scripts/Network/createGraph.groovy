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
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
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
package org.orbisgis.orbiswps.scripts.scripts.Network

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/**
 * This process creates a graph network.
 * @author Erwan Bocher
 * @author Sylvain PALOMINOS
 */
@Process(
        title = ["Create a graph","en",
                "Créer un graphe","fr"],
        description = [
                "Create a graph stored in two tables nodes and edges from an input table that contains Multi or LineString.<br>If the input table has name 'input', then the output tables are named 'input_nodes' and 'input_edges'.","en",
                "Créer un graphe stocké dans deux tables 'nodes' (noeuds) et 'edges' (arcs) depuis une table contenant des objets du type MultiLineString et LineString.<br>Si la table en entrée a pour nom 'input', alors celles en sortie seront nommées 'input_nodes' et 'input_edges'.","fr"],
        keywords = ["Network,Geometry","en",
                "Réseau,Géometrie","fr"],
        properties = ["DBMS_TYPE", "H2GIS"],
        version = "1.0")
def processing() {    
    if(slope==null){
        slope=false;
    }
	
    String query = " SELECT ST_GRAPH('"   + inputJDBCTable + "', '"+geometricField[0]+"',"+tolerance+ ", "+ slope+ ")"

    //Execute the query
    sql.execute(query)
    
    if(dropInputTable){
        sql.execute "drop table if exists " + inputJDBCTable
    }

    literalOutput = "The graph network has been created."
}

/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input data source. */
@JDBCTableInput(
        title = ["Input spatial data","en","Donnée spatiale d'entrée","fr"],
        description = [
                "The spatial data source to create the graphe tables.","en",
                "La source de données spatiales servant à la création des tables du graphe.","fr"],
        dataTypes = ["LINESTRING", "MULTILINESTRING"])
String inputJDBCTable


/** Name of the Geometric field of the JDBCTable inputJDBCTable. */
@JDBCColumnInput(
        title = ["Geometric column","en",
                "Colonne géométrique","fr"],
        description = [
                "The geometric column of the data source.","en",
                "La colonne géométrique de la source de données.","fr"],
        jdbcTableReference = "inputJDBCTable",
        dataTypes = ["LINESTRING", "MULTILINESTRING"])
String[] geometricField

/** Snapping tolerance. */
@LiteralDataInput(
        title = ["Snapping tolerance","en",
                "Tolérance d'accrochage","fr"],
        description = [
                "The tolerance value is used specify the side length of a square Envelope around each node used to snap together other nodes within the same Envelope.","en",
                "La valeur de tolérance est utilisée pour fixer la taille du coté du carré de l'enveloppe autour de chaque noeud  qui est utilisée pour rassembler les noeuds appartenant à la meme enveloppe.","fr"])
Double tolerance 

@LiteralDataInput(
        title = ["Slope orientation ?","en",
                "Orientation selon la pente ?","fr"],
        description = ["True if edges should be oriented by the z-value of their first and last coordinates (decreasing).","en",
                "Vrai si les sommets doivent etre orientés selon les valeurs Z de leur première et dernière coordonnées.","fr"],
	    minOccurs = 0)
Boolean slope

@LiteralDataInput(
    title = [
				"Drop the input table","en",
				"Supprimer la table d'entrée","fr"],
    description = [
				"Drop the input table when the script is finished.","en",
				"Supprimer la table d'entrée lorsque le script est terminé.","fr"])
Boolean dropInputTable 


/** String output of the process. */
@LiteralDataOutput(
        title = ["Output message", "en",
                "Message de sortie", "fr"],
        description = ["The output message.", "en",
                "Le message de sortie.", "fr"])
String literalOutput



