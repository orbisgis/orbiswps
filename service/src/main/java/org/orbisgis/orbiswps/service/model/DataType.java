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
package org.orbisgis.orbiswps.service.model;

import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Types;


/**
 * Enumeration of the LiteralData type.
 *
 * For more information : http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html
 *
 * @author Sylvain PALOMINOS
 */

public enum DataType {
    //LiteralData types
    NUMBER("number"),
    INTEGER("https://www.w3.org/2001/XMLSchema#integer"),
    DOUBLE("https://www.w3.org/2001/XMLSchema#double"),
    FLOAT("https://www.w3.org/2001/XMLSchema#float"),
    SHORT("https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#short"),
    BYTE("https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#byte"),
    UNSIGNED_BYTE("https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#unsignedByte"),
    LONG("https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#long"),
    STRING("https://www.w3.org/2001/XMLSchema#string"),
    BOOLEAN("https://www.w3.org/2001/XMLSchema#boolean"),

    //Other types
    OTHER("OTHER"),
    GEOMETRY("GEOMETRY"),
    POINT("POINT"),
    LINESTRING("LINESTRING"),
    POLYGON("POLYGON"),
    MULTIPOINT("MULTIPOINT"),
    MULTILINESTRING("MULTILINESTRING"),
    MULTIPOLYGON("MULTIPOLYGON"),
    GEOMCOLLECTION("GEOMCOLLECTION"),
    GEOMETRYZ("GEOMETRYZ"),
    POINTZ("POINTZ"),
    LINESTRINGZ("LINESTRINGZ"),
    POLYGONZ("POLYGONZ"),
    MULTIPOINTZ("MULTIPOINTZ"),
    MULTILINESTRINGZ("MULTILINESTRINGZ"),
    MULTIPOLYGONZ("MULTIPOLYGONZ"),
    GEOMCOLLECTIONZ("GEOMCOLLECTIONZ"),
    GEOMETRYM("GEOMETRYM"),
    POINTM("POINTM"),
    LINESTRINGM("LINESTRINGM"),
    POLYGONM("POLYGONM"),
    MULTIPOINTM("MULTIPOINTM"),
    MULTILINESTRINGM("MULTILINESTRINGM"),
    MULTIPOLYGONM("MULTIPOLYGONM"),
    GEOMCOLLECTIONM("GEOMCOLLECTIONM"),
    GEOMETRYZM("GEOMETRYZM"),
    POINTZM("POINTZM"),
    LINESTRINGZM("LINESTRINGZM"),
    RASTER("RASTER"),
    NONE("NONE");

    /** URI for the model type. */
    private URI uri;

    /**
     * Main constructor.
     * @param uriString String of the URI to the reference of the type.
     */
    DataType(String uriString) {
        try {
            this.uri = new URI(uriString);
        } catch (URISyntaxException e) {
            LoggerFactory.getLogger(DataType.class).error(e.getMessage());
        }
    }

    /**
     * Return true is the type is spatial, false otherwise.
     * @param dataType DataType to test.
     * @return True if the given model type is spatial, false otherwise.
     */
    public static boolean isSpatialType(DataType dataType){
        return dataType.equals(GEOMETRY) ||
                dataType.equals(POINT) ||
                dataType.equals(LINESTRING) ||
                dataType.equals(POLYGON) ||
                dataType.equals(MULTIPOINT) ||
                dataType.equals(MULTILINESTRING) ||
                dataType.equals(MULTIPOLYGON) ||
                dataType.equals(GEOMCOLLECTION) ||
                dataType.equals(GEOMETRYZ) ||
                dataType.equals(POINTZ) ||
                dataType.equals(LINESTRINGZ) ||
                dataType.equals(POLYGONZ) ||
                dataType.equals(MULTIPOINTZ) ||
                dataType.equals(MULTILINESTRINGZ) ||
                dataType.equals(MULTIPOLYGONZ) ||
                dataType.equals(GEOMCOLLECTIONZ) ||
                dataType.equals(GEOMETRYM) ||
                dataType.equals(POINTM) ||
                dataType.equals(LINESTRINGM) ||
                dataType.equals(POLYGONM) ||
                dataType.equals(MULTIPOINTM) ||
                dataType.equals(MULTILINESTRINGM) ||
                dataType.equals(MULTIPOLYGONM) ||
                dataType.equals(GEOMCOLLECTIONM) ||
                dataType.equals(GEOMETRYZM) ||
                dataType.equals(POINTZM) ||
                dataType.equals(LINESTRINGZM);
    }

    /**
     * Return true is the type is spatial with Z dimension, false otherwise.
     * @param dataType DataType to test.
     * @return True if the given model type is spatial, false otherwise.
     */
    public static boolean isSpatialZType(DataType dataType){
        return dataType.equals(GEOMETRYZ) ||
                dataType.equals(POINTZ) ||
                dataType.equals(LINESTRINGZ) ||
                dataType.equals(POLYGONZ) ||
                dataType.equals(MULTIPOINTZ) ||
                dataType.equals(MULTILINESTRINGZ) ||
                dataType.equals(MULTIPOLYGONZ) ||
                dataType.equals(GEOMCOLLECTIONZ) ||
                dataType.equals(GEOMETRYZM) ||
                dataType.equals(POINTZM) ||
                dataType.equals(LINESTRINGZM);
    }

    /**
     * Return true is the type is spatial with M dimension, false otherwise.
     * @param dataType DataType to test.
     * @return True if the given model type is spatial, false otherwise.
     */
    public static boolean isSpatialMType(DataType dataType){
        return dataType.equals(GEOMETRYM) ||
                dataType.equals(POINTM) ||
                dataType.equals(LINESTRINGM) ||
                dataType.equals(POLYGONM) ||
                dataType.equals(MULTIPOINTM) ||
                dataType.equals(MULTILINESTRINGM) ||
                dataType.equals(MULTIPOLYGONM) ||
                dataType.equals(GEOMCOLLECTIONM) ||
                dataType.equals(GEOMETRYZM) ||
                dataType.equals(POINTZM) ||
                dataType.equals(LINESTRINGZM);
    }

    /**
     * Return true is the type is spatial with Z and M dimension, false otherwise.
     * @param dataType DataType to test.
     * @return True if the given model type is spatial, false otherwise.
     */
    public static boolean isSpatialZMType(DataType dataType){
        return dataType.equals(GEOMETRYZM) ||
                dataType.equals(POINTZM) ||
                dataType.equals(LINESTRINGZM);
    }

    /**
     * Returns true is the DataType is a number, false otherwise.
     * @param dataType DataType to test.
     * @return True is the DataType is a number, false otherwise.
     */
    public static boolean isNumber(DataType dataType){
        return dataType.equals(INTEGER) ||
                dataType.equals(DOUBLE) ||
                dataType.equals(FLOAT) ||
                dataType.equals(SHORT) ||
                dataType.equals(BYTE) ||
                dataType.equals(UNSIGNED_BYTE) ||
                dataType.equals(LONG);
    }

    /**
     * Return the DataType corresponding to the string representation of the field type, no matter the case.
     * @param fieldType String representation of the field type.
     * @return The DataType corresponding to the field type.
     */
    public static DataType getDataTypeFromFieldType(String fieldType){
        return DataType.valueOf(fieldType.toUpperCase());
    }

    /**
     * Returns the URI of the type.
     * @return The URI of the type.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the DataType corresponding to the given database type name.
     * @param dbTypeName Name of the model type from the database.
     * @return The DataType corresponding to the given database type name.
     */
    public static DataType getDataType(String dbTypeName){
        switch(dbTypeName.toUpperCase()) {
            case "INT":
            case "INTEGER":
            case "MEDIUMINT":
            case "INT4":
            case "SIGNED":
            case "SERIAL":
            case "SERIAL4":
                return INTEGER;
            case "BOOLEAN":
            case "BIT":
            case "BOOL":
                return BOOLEAN;
            case "TINYINT":
                return BYTE;
            case "SMALLINT":
            case "INT2":
            case "YEAR":
            case "SERIAL2":
            case "SMALLSERIAL":
                return SHORT;
            case "BIGINT":
            case "INT8":
            case "IDENTITY":
            case "BIGSERIAL":
            case "SERIAL8":
                return LONG;
            case "DOUBLE":
            case "FLOAT":
            case "FLOAT8":
                return DOUBLE;
            case "REAL":
            case "FLOAT4":
                return FLOAT;
            case "VARCHAR":
            case "LONGVARCHAR":
            case "VARCHAR2":
            case "NVARCHAR":
            case "NVARCHAR2":
            case "VARCHAR_CASESENSITIVE":
            case "VARCHAR_IGNORECASE":
            case "CHAR":
            case "CHARACTER":
            case "NCHAR":
                return STRING;
            case "NUMBER":
            case "NUMERIC": return NUMBER;
            case "OTHER": return OTHER;
            case "GEOMETRY": return GEOMETRY;
            case "POINT": return POINT;
            case "LINESTRING": return LINESTRING;
            case "POLYGON": return POLYGON;
            case "MULTIPOINT": return MULTIPOINT;
            case "MULTILINESTRING": return MULTILINESTRING;
            case "MULTIPOLYGON": return MULTIPOLYGON;
            case "GEOMCOLLECTION": return GEOMCOLLECTION;
            case "GEOMETRYZ": return GEOMETRYZ;
            case "POINTZ": return POINTZ;
            case "LINESTRINGZ": return LINESTRINGZ;
            case "POLYGONZ": return POLYGONZ;
            case "MULTIPOINTZ": return MULTIPOINTZ;
            case "MULTILINESTRINGZ": return MULTILINESTRINGZ;
            case "MULTIPOLYGONZ": return MULTIPOLYGONZ;
            case "GEOMCOLLECTIONZ": return GEOMCOLLECTIONZ;
            case "GEOMETRYM": return GEOMETRYM;
            case "POINTM": return POINTM;
            case "LINESTRINGM": return LINESTRINGM;
            case "POLYGONM": return POLYGONM;
            case "MULTIPOINTM": return MULTIPOINTM;
            case "MULTILINESTRINGM": return MULTILINESTRINGM;
            case "MULTIPOLYGONM": return MULTIPOLYGONM;
            case "GEOMCOLLECTIONM": return GEOMCOLLECTIONM;
            case "GEOMETRYZM": return GEOMETRYZM;
            case "POINTZM": return POINTZM;
            case "LINESTRINGZM": return LINESTRINGZM;
            case "RASTER": return RASTER;
            default: return DataType.valueOf(dbTypeName.toUpperCase());
        }
    }

    /**
     * Returns the DataType corresponding to the given integer database geometric type.
     * It only take into account the geometric types.
     * @param geometryType Integer geometric type from the database.
     * @return The DataType corresponding to the given integer database geometric type.
     */
    public static DataType getGeometryType(int geometryType){
        switch(geometryType) {
            case GeometryTypeCodes.GEOMETRY: return GEOMETRY;
            case GeometryTypeCodes.POINT: return POINT;
            case GeometryTypeCodes.LINESTRING: return LINESTRING;
            case GeometryTypeCodes.POLYGON: return POLYGON;
            case GeometryTypeCodes.MULTIPOINT: return MULTIPOINT;
            case GeometryTypeCodes.MULTILINESTRING: return MULTILINESTRING;
            case GeometryTypeCodes.MULTIPOLYGON: return MULTIPOLYGON;
            case GeometryTypeCodes.GEOMCOLLECTION: return GEOMCOLLECTION;
            case GeometryTypeCodes.GEOMETRYZ: return GEOMETRYZ;
            case GeometryTypeCodes.POINTZ: return POINTZ;
            case GeometryTypeCodes.LINESTRINGZ: return LINESTRINGZ;
            case GeometryTypeCodes.POLYGONZ: return POLYGONZ;
            case GeometryTypeCodes.MULTIPOINTZ: return MULTIPOINTZ;
            case GeometryTypeCodes.MULTILINESTRINGZ: return MULTILINESTRINGZ;
            case GeometryTypeCodes.MULTIPOLYGONZ: return MULTIPOLYGONZ;
            case GeometryTypeCodes.GEOMCOLLECTIONZ: return GEOMCOLLECTIONZ;
            case GeometryTypeCodes.GEOMETRYM: return GEOMETRYM;
            case GeometryTypeCodes.POINTM: return POINTM;
            case GeometryTypeCodes.LINESTRINGM: return LINESTRINGM;
            case GeometryTypeCodes.POLYGONM: return POLYGONM;
            case GeometryTypeCodes.MULTIPOINTM: return MULTIPOINTM;
            case GeometryTypeCodes.MULTILINESTRINGM: return MULTILINESTRINGM;
            case GeometryTypeCodes.MULTIPOLYGONM: return MULTIPOLYGONM;
            case GeometryTypeCodes.GEOMCOLLECTIONM: return GEOMCOLLECTIONM;
            case GeometryTypeCodes.GEOMETRYZM: return GEOMETRYZM;
            case GeometryTypeCodes.POINTZM: return POINTZM;
            case GeometryTypeCodes.LINESTRINGZM: return LINESTRINGZM;
            default: return GEOMETRY;
        }
    }

    /**
     * Returns the DataType corresponding to the given integer database model type.
     * It doesn't take into account the geometric types.
     * @param sqlTypeId Integer model type from the database.
     * @return The DataType corresponding to the given integer database model type.
     */
    public static DataType getDataType(int sqlTypeId) {
        switch (sqlTypeId) {
            case Types.BOOLEAN:
            case Types.BIT:
                return BOOLEAN;
            case Types.DOUBLE:
                return DOUBLE;
            case Types.NUMERIC:
                return NUMBER;
            case Types.DECIMAL:
            case Types.REAL:
            case Types.FLOAT:
                return FLOAT;
            case Types.BIGINT:
            case Types.INTEGER:
                return INTEGER;
            case Types.SMALLINT:
                return SHORT;
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.CHAR:
                return STRING;
            default:
                return OTHER;
        }
    }

    /**
     * Test if the given dataType is equivalent to this one.
     * @param dataTypeToTest DataType to test.
     * @return True if the DataType are equivalent, false otherwise.
     */
    public boolean isDataTypeEquivalent(DataType dataTypeToTest){
        if(this.equals(dataTypeToTest)){
            return true;
        }
        else{
            switch(this) {
                case GEOMETRY: return DataType.isSpatialType(dataTypeToTest);
                case POINT: return dataTypeToTest.equals(POINTZ)||dataTypeToTest.equals(POINTM)||dataTypeToTest.equals(POINTZM);
                case LINESTRING: return dataTypeToTest.equals(LINESTRINGZ)||dataTypeToTest.equals(LINESTRINGM)||dataTypeToTest.equals(LINESTRINGZM);
                case POLYGON: return dataTypeToTest.equals(POLYGONZ)||dataTypeToTest.equals(POLYGONM);
                case MULTIPOINT: return dataTypeToTest.equals(MULTIPOINTZ)||dataTypeToTest.equals(MULTIPOINTM);
                case MULTILINESTRING: return dataTypeToTest.equals(MULTILINESTRINGZ)||dataTypeToTest.equals(MULTILINESTRINGM);
                case MULTIPOLYGON: return dataTypeToTest.equals(MULTIPOLYGONZ)||dataTypeToTest.equals(MULTIPOLYGONM);
                case GEOMCOLLECTION: return dataTypeToTest.equals(MULTIPOINT)||dataTypeToTest.equals(MULTIPOINTZ)||dataTypeToTest.equals(MULTIPOINTM)||
                        dataTypeToTest.equals(MULTILINESTRING)||dataTypeToTest.equals(MULTILINESTRINGZ)||dataTypeToTest.equals(MULTILINESTRINGM)||
                        dataTypeToTest.equals(MULTIPOLYGON)||dataTypeToTest.equals(MULTIPOLYGONZ)||dataTypeToTest.equals(MULTIPOLYGONM);

                case GEOMETRYZ: return isSpatialZType(dataTypeToTest);
                case POINTZ: return dataTypeToTest.equals(POINTZM);
                case LINESTRINGZ: return dataTypeToTest.equals(LINESTRINGZM);
                case GEOMCOLLECTIONZ: return dataTypeToTest.equals(MULTIPOINTZ)||dataTypeToTest.equals(MULTILINESTRINGZ)||dataTypeToTest.equals(MULTIPOLYGONZ);

                case GEOMETRYM: return isSpatialMType(dataTypeToTest);
                case POINTM: return dataTypeToTest.equals(POINTZM);
                case LINESTRINGM: return dataTypeToTest.equals(LINESTRINGZM);
                case GEOMCOLLECTIONM: return dataTypeToTest.equals(MULTIPOINTM)||dataTypeToTest.equals(MULTILINESTRINGM)||dataTypeToTest.equals(MULTIPOLYGONM);
            }
        }
        return false;
    }

    /**
     * Geometry type codes as defined in SFS 1.2.1 from the OGC.
     */
    private interface GeometryTypeCodes{
        int GEOMETRY = 0;
        int POINT = 1;
        int LINESTRING = 2;
        int POLYGON = 3;
        int MULTIPOINT = 4;
        int MULTILINESTRING = 5;
        int MULTIPOLYGON = 6;
        int GEOMCOLLECTION = 7;
        int GEOMETRYZ = 1000;
        int POINTZ = POINT + 1000;
        int LINESTRINGZ = LINESTRING + 1000;
        int POLYGONZ = POLYGON + 1000;
        int MULTIPOINTZ = MULTIPOINT + 1000;
        int MULTILINESTRINGZ = MULTILINESTRING + 1000;
        int MULTIPOLYGONZ = MULTIPOLYGON + 1000;
        int GEOMCOLLECTIONZ = GEOMCOLLECTION + 1000;
        int GEOMETRYM = 2000;
        int POINTM = POINT + 2000;
        int LINESTRINGM = LINESTRING + 2000;
        int POLYGONM = POLYGON + 2000;
        int MULTIPOINTM = MULTIPOINT + 2000;
        int MULTILINESTRINGM = MULTILINESTRING + 2000;
        int MULTIPOLYGONM = MULTIPOLYGON + 2000;
        int GEOMCOLLECTIONM = GEOMCOLLECTION + 2000;
        int GEOMETRYZM = 3000;
        int POINTZM = POINT + 3000;
        int LINESTRINGZM = LINESTRING + 3000;
    }
}
