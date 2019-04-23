# NP-Likeness scorer - Database Filler

Author: Maria Sorokina, maria.sorokina@uni-jena.de
Last modified: 23.04.2019

Natural products (NPs), often also referred to as secondary metabolites, are small molecules synthesized by living organisms. Natural products are of interest due to their bioactivity and in this context as starting points for the development of drugs and other bioactive synthetic products. In 2008, Ertl and coworkers suggested a natural product (NP) likeness score, which, for a given molecule its similarity to the structure space covered by NPs compared to the structure space covered by synthetic molecules (SM). Such a score could be used to prioritize compounds from virtual libraries. Later, Jayaseelan and coauthors presented a pure open data, open source version of the NP-likeness scorer  (Ertl et al., 2008; Jayaseelan et al., 2012).

This archive contains the first part of the NP-likeness software suite NaPLeS (Natural Products Likeness Scorer). The suite consists of a web application that allows computing the NP-likeness score online, of an easy to install local scorer to compute NP-likeness for big datasets (this archive) and a MySQL database containing a large number of NPs from diverse public databases with pre-computed NP-likeness scores and other metrics (the NPLikenessDB.sql file in this archive).

The NaPLeS web application is available at naples.naturalproducts.net


This directory contains:
- a Dockerfile to build to image of the NPdatabaseFiller java app
- a docker-compose.yml file allowing to build the docker-compose environment (the Java app and the MySQL database)
- target directory containing the NPdatabaseFiller application jar and a helper jar
- nplsmol directory containing the molecular data. This directory is a volume for the NPdatabaseFiller Docker container
- this README

The NPdatabaseFiller application has three modes, allowing to compute the NP-likeness scores for molecules from scratch, to add new molecules to the database and to compute their NP-likeness scores based on the precomputed fragment frequencies, and to update the NP-likeness scors (this re-computes all fragment frequencies then recomputes all NP-likeness scores).

The mode can be changed by editing the docker-compose.yml file in the "commands" part:

1. generate all scores and the whole database from scratch; command: 
```
/nplsmol/molecular_file_locations.txt fromScratch
```
(where the first argument is the file precising molecular files to input, their source and status (NP, SM or BIOGENIC))

2. compute NP-likeness scores only for one file (containing up to 500 000 molecules); command:
```
file.sdf SOURCE NP addNewData
```
(first argument: molecular file (SDF, MOL or SMI), second argument: source (database name for example), third argument: status (NP, SM or BIOGENIC), fourth argument: "addNewData" tag)

3. update all scores (after insertion of a big number of molecules for example, or a reimplementation of the NP-likeness formula, etc)
command: "updateScores"

Note: The modes 2 and 3 recquire a running and filled 'npdatabasefiller_npls-mysql-db_1' container

A typical usage is to first run the first mode:
```
$ docker-compose build (with the "fromScratch" command)
$ docker-compose up -d
```

When the calculations are over, the java container ('npdatabasefiller_npls-db-filler_1' by default) will stop automatically.
To launch step 2 or step 3, edit the docker-compose.yml file appropriately, then rebuild a`nd relaunch the docker compose as following (without putting down the mysql container):

```
$ docker-compose up -d --no-deps --build npls-db-filler
```


The unarchived source code for this project is available at https://github.com/mSorok/NPdatabaseFiller and can be recompiled as a maven project.

