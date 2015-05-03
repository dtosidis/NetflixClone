package ase.task4.netflix

import java.util.List;
import java.util.Map;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

class NetflixDSL extends FactoryBuilderSupport {

	
	NetflixDSL() {
		super(true)
	}
	
	def DistributorsNamesMap = [:]
	def SongsNamesMap = [:]
	def PlaylistsMap = [:]
	
	def register() {
        registerBeanFactory("netflix", Netflix.class)
        registerFactory("Distributors:", new TransparentAggregate(expects : [ DistributorCompany.class ]))
		registerFactory("Library:", new TransparentAggregate(expects : [ Songs.class ]))
		registerFactory("Playists:", new TransparentAggregate(expects : [Songs.class, Plist.class ]))
		registerFactory("distributor", new DistributorCompanyFactory())
		registerFactory("song", new SongFactory())
		registerFactory("name", new PlaylistFactory())
    }

	
    private class DistributorCompanyFactory extends AbstractFactory {

        @Override
        Object newInstance(FactoryBuilderSupport fbs, Object name, Object value, Map features) throws InstantiationException, IllegalAccessException {
			
			/*
			 * Checks if the distributor's name exists
			 * */
			def dis_name = fbs.getCurrent().distributors[value as String]
			assert dis_name == null , "Distributors name must be unique"
			
			DistributorsNamesMap.put(value, features)
			features += [ name : value ]
			new DistributorCompany(features)
        }

        @Override
        void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
            assert parent instanceof Netflix
            parent << child
        }
        @Override
        boolean isLeaf() {
            true
        }
    }

    private class SongFactory extends AbstractFactory {

        @Override
        Object newInstance(FactoryBuilderSupport fbs, Object name, Object value, Map features) throws InstantiationException, IllegalAccessException {
			SongsNamesMap.put(value, features)
			features += [ name : value ]
			
			/*
			 * Checks if the distributor's name exists
			 * */
			def dist = features.get("produced_by")
			def new_dist = fbs.getCurrent().distributors[dist as String]
			assert new_dist != null,  "given distributor ${dist} is undefined"
			
			new Songs(features)
           
        }

        @Override
        void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
            assert parent instanceof Netflix
            parent << child
        }
        @Override
        boolean isLeaf() {
            true
        }
    }

    private class PlaylistFactory extends AbstractFactory {
        @Override
        Object newInstance(FactoryBuilderSupport fbs, Object name, Object value, Map features) throws InstantiationException, IllegalAccessException {
			PlaylistsMap.put(value, features)
			features += [ name : value ]
			
			/*
			 * Checks if the playlist's name exists
			 * */
            def pl_name = fbs.getCurrent().playlists[value as String]
            assert pl_name == null , "Playlist name must be unique"
			

			/*
			 * Checks if there are included playlists and if they exist
			 * */
			def incl_plists = features.get("playlist")
			for (p in incl_plists) {
				 def pl_lookup = fbs.getCurrent().playlists[p as String]
				 assert pl_lookup != null,  "Incuded playlist ${p} is undefined"
			}
			
			/*
			 * Checks if the included songs exist in the library
			 * */
			def incl_songs = features.get("consists_of")
			for (s in incl_songs) {
				def song_lookup = fbs.getCurrent().library[s as String]
				assert song_lookup != null,  "Incuded song ${s} is undefined"
			}
			
			/*
			 * Checks if there are songs to be excluded from playlists
			 * and whether they refer to existing songs-playlists
			 * */
			def without = features.get("without")
			without.each{k,v ->
				def pl_lookup = fbs.getCurrent().playlists[k as String]
				assert pl_lookup != null,  "Incuded playlist ${k} is undefined"
				for (s in v) {
					def song_lookup = fbs.getCurrent().library[s as String]
					assert song_lookup != null,  "Incuded song ${s} is undefined"
				}
			}
			new Plist(features)
        }
  
		
		@Override
		void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
			assert parent instanceof Netflix
			parent << child
		}
		@Override
		boolean isLeaf() {
			true
		}
		
	}

    /**
     * No-op block for grouping.
     */
    private class TransparentAggregate extends AbstractFactory {
        private List expects = [ Object.class ]

        @Override
        Object newInstance(FactoryBuilderSupport fbs, Object name, Object value, Map features) throws InstantiationException, IllegalAccessException {
            fbs.getCurrent() // before construction we are still in parent's context
        }

        @Override
        void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
            assert expects.any { it.isInstance(child) } : "illegal member for this aggregate, expected ${clazz.getSimpleName()}"
        }
    }
}