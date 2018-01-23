/*A script to load some data */

DROP TABLE IF EXISTS input_table_a, input_table_b;

CREATE TABLE input_table_a(the_geom LINESTRING, id integer, type varchar);

INSERT INTO input_table_a VALUES ((ST_GeomFromText('LINESTRING (113 155, 220 160)',2154)), 2, 'OrbisGIS');



CREATE TABLE input_table_b(the_geom POLYGON, id integer, code varchar);

INSERT INTO input_table_b VALUES ((ST_GeomFromText('POLYGON ((140 180, 180 180, 180 140, 140 140, 140 180))',2154)), 1, 'h2gis');
INSERT INTO input_table_b VALUES ((ST_GeomFromText('POLYGON ((130 270, 190 270, 190 230, 130 230, 130 270))',2154)), 2, 'CNRS');
INSERT INTO input_table_b VALUES ((ST_GeomFromText('POLYGON ((220 220, 260 220, 260 180, 220 180, 220 220))',2154)), 3, 'Vannes');
