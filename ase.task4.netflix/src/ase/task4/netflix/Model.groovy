package ase.task4.netflix


import groovy.time.TimeDuration
import groovy.transform.Canonical

/**
 * netflix dsl model
 */

@Canonical class Netflix {

    static Netflix netflix() { return new Netflix() }

    def distributors = [:]
    def library = [:]
    def playlists = [:]


    Netflix leftShift(DistributorCompany distributor) {
        this.distributors[distributor.name] = distributor
        return this
    }

	
    Netflix leftShift(Songs lib) {
        this.library[lib.name] = lib
        return this
    }
	
	Netflix leftShift(Plist plist) {
		this.playlists[plist.name] = plist
		return this
	}
    
}


@Canonical class Plist {
	
		String name
		List<String> consists_of
		List<String> playlist
		Map without
		
}

@Canonical class DistributorCompany {

    String name
    String Address
    String IBAN
	String BIC
}

@Canonical class Songs {

    String name
    String sung_by
	String produced_by
	String length
    Genre genre
	Float price
}

// helper

enum Genre {

	POP, ROCK, HARDROCK, CLASSIC, FOLK, OTHER
}
