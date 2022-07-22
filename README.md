# vbb-gtfs-clj

Tools for loading (currently only [VBB](https://vbb-gtfs.jannisr.de/)) GTFS data into a [Datahike](https://github.com/replikativ/datahike) database and (not yet implemented) do other things of interest to me. Could evolve drastically.

Currently, only data download and some CSV processing is implemented; database loading isn't quite done yet. 

## Usage

Download, preprocess CSV, and (WIP) load into Datahike:
    
    $ ./bin/setup.sh 
    
Only preprocess CSV and (WIP) load into Datahike; assumes `RESOURCE_DIR` correctly set and files present there:

    $ clj -X vbb-gtfs-clj.core/setup

## License

Copyright © 2022 Yee Fay Lim

Distributed under the Eclipse Public License version 1.0.
