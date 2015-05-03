package ase.task4.netflix

//def builder = new PoorMansTagBuilder20()
//def distributors = builder.Distributors {
//	UniversalMusic {
//		Address("56 Some Street, 12345 New York, NY, US")
//		"Account Information:" {
//			IBAN("US 12 123 123 123")
//			BIC("SOMEUSX1")
//		}
//	}
//	SonyMusic {
//		Address("56 Some Street, 12345 New York, NY, US")
//		"Account Information:" {
//			IBAN("US 12 123 123 123")
//			BIC("SOMEUSX1")
//		}
//	}
//	AllegroMusic {
//		Address("56 Some Street, 12345 New York, NY, US")
//		"Account Information:" {
//			IBAN("US 12 123 123 123")
//			BIC("SOMEUSX1")
//		}
//	}
//}

import java.lang.invoke.LambdaForm.Name;

import groovy.time.TimeCategory
import static Genre.*
import groovy.xml.MarkupBuilder


def builder = new NetflixDSL()

def x = builder.netflix {
	Distributors: {
		distributor "UniversalMusic", Address:"JP 12 34 43 55 93", IBAN: "JP 12 34 43 55 93", BIC: "SOMEJPJ1"
		distributor "SonyMusic", Address:"1 Sony Street, 2345 Tokia, JP", IBAN: "JT 12 34 43 55 93", BIC: "SOMEJPJ2"
		distributor "AllegroMusic", Address:"4 Allegro Street, 2345 Tokia, JP", IBAN: "JP 12 34 43 55 95", BIC: "SOMEJPJ3"
		distributor "OdeonMusic", Address:"26 Another Street, 12345 New York, NY, US", IBAN: "JP 56 34 43 55 95", BIC: "SOMEJPJ4"
	}
	Library: {
		song "Bohemian_Rapsody", sung_by: "Queen", produced_by: "OdeonMusic", length: "5:32", genre: ROCK, price: 3.65
		song "Get_lucky", sung_by: "Daft Punk", produced_by: "SonyMusic",length: "3:01",genre: OTHER, price: 4.32
		song "Song2", sung_by: "Blur", produced_by: "UniversalMusic",length: "4:23",genre: ROCK, price: 3.45
		song "Come_as_you_are",	sung_by: "Nirvana", produced_by: "AllegroMusic",length: "7:23", genre: CLASSIC, price: 7.75
		song "Roar", sung_by: "Katie Perry", produced_by: "SonyMusic", length: "2:35",genre: POP, price: 4.67
		song "No_One", sung_by:"Alicia Keys", produced_by: "AllegroMusic",length: "4:35",genre: FOLK,price: 5.85
		song "Call_Me_Maybe", sung_by: "Carly Rae Jepsen", produced_by: "OdeonMusic", length: "4:45", genre: CLASSIC, price: 5.67
		song "Shadow_Dancing", sung_by: "Andy Gibb", produced_by: "OdeonMusic", length: "4:21", genre: FOLK, price: 3.47
		song "I_Want_to_Hold_Your_Hand", sung_by: "The Beatles", produced_by: "AllegroMusic", length: "4:35",	genre: CLASSIC,	price: 8.67
		song "Its_All_in_the_Game",sung_by: "Tommy Edwards",produced_by: "SonyMusic",length: "6:35",genre: HARDROCK,price: 7.67
		song "How_You_Remind_Me", sung_by: "Nickelback", produced_by: "UniversalMusic",length: "2:35", genre: POP, price: 4.23
	}
	Playlists:{
		name "SimpleList", consists_of: ["Bohemian_Rapsody","Come_as_you_are","Get_lucky","Roar"]
		name "ComplexList", consists_of: ["Song2","I_Want_to_Hold_Your_Hand","Its_All_in_the_Game"], playlist:["SimpleList"], without: ["SimpleList":["Get_lucky","Roar"]]
		name "RockList", consists_of: ["How_You_Remind_Me","Call_Me_Maybe","Shadow_Dancing"], playlist:["ComplexList"]
		name "OtherList", consists_of: ["No_One","How_You_Remind_Me"]
		name "AnotherList", consists_of: ["I_Want_to_Hold_Your_Hand"], playlist:["SimpleList", "OtherList" , "RockList"], without: ["SimpleList":["Get_lucky","Roar"], "OtherList":["How_You_Remind_Me"], "RockList":["Call_Me_Maybe"]]
	}
}




/*
 * Pretty print into the instance.txt file of the above created instance
 * 
 * */


PrintWriter writer = null

File instance = new File("instance.txt")
writer = new PrintWriter(instance)
writer.println("Distributors:")
for ( e in builder.DistributorsNamesMap ) {
	writer.println("\t${e.key}")
	def value = e.value
	value.each{ k, v -> if (k=="IBAN"){
		writer.println( "\tAccount Information:")
		writer.println( "\t\t${k}:${v}")} 
		else if (k=="BIC") {writer.println( "\t\t${k}:${v}")}
		else {writer.println( "\t${k}:${v}") }}
	
	writer.println( "\n")
}
writer.println ("Library:")
for ( e in builder.SongsNamesMap ) {
	writer.println ("\t${e.key}")
	def value = e.value
	value.each{ k, v -> writer.println ("\t${k}:${v}") }
	writer.println("\n")
}

writer.println ("Playlists:")
for ( e in builder.PlaylistsMap ) {
	writer.println ("\t${e.key}")
	def value = e.value
	value.each{ k, v -> writer.println ("\t${k}:${v}") }
	writer.println ("\n")
}
writer.close()



/*
 * 
 * Getting all the songs belonging to each playlist 
 * and printing them into tables per playlist in the
 * songs_per_playlist.html file
 * 
 * */


def find_songs_recursive(plmap, playlist, songs, remove){

	for (pl in playlist){
		if (plmap.get(pl).get("consists_of")){
			for (s in plmap.get(pl).get("consists_of")){
				songs.add(s)
			}			
		}
		if (plmap.get(pl).get("without")){
			plmap.get(pl).get("without").each { k, val ->
				for (v in val) remove.add(v)
			}
		}
		if (plmap.get(pl).get("playlist")){
			find_songs_recursive(plmap, plmap.get(pl).get("playlist"), songs, remove)
		}
	}
	return [songs, remove]
}



def total_songs = []
def songs = []
def remove = []

StringWriter html_writer1 = new StringWriter()
def build1 = new MarkupBuilder(html_writer1)
build1.html{
head{
 style(type:"text/css", '''
    table {
		    width:30%;
 			margin-bottom:3%;
		}
		table, th, td {
		    border: 1px solid black;
		    border-collapse: collapse;
		}
		th, td {
		    padding: 5px;
		    text-align: left;
		}
		table#t01 tr:nth-child(even) {
		    background-color: #eee;
		}
		table#t01 tr:nth-child(odd) {
		   background-color:#fff;
		}
		table#t01 th	{
		    background-color: black;
		    color: white;
		} 
    ''')  
}
body{
 h1("Songs per playlist")
 
	   for ( e in builder.PlaylistsMap ) {
		   table('id':"t01"){
			   tr{
		   songs= []
		   remove= []
		   th("${e.key}")}
		   def value = e.value
		   value.each{ k, v ->
			   if (k=="consists_of"){
				   for (s in v) songs.add(s)
			   }
			   if (k=="playlist"){
				   def result = find_songs_recursive(builder.PlaylistsMap, v, [], [])
				   for (r in result[0])songs.add(r)
				   remove = result[1]
			   }
			   if (k=="without"){
				   v.each { pl, val ->
					   for (w in val) remove.add(w)
				   }
			   }
		   }
		   
		   for(r in remove)songs.remove(r)
		   for (s in songs) tr{td(s)}
		   for(s in songs)total_songs.add(s)
		  
	   }
	 
   }
 }

}
new File("songs_per_playlist.html").write(html_writer1.toString())


/*
 * Earnings calculation per distributor
 * and printing them into the distributors_earnings.html
 * file
 * 
 * */

def Earnings = [:]
for (s in total_songs){
	price = builder.SongsNamesMap.get(s).get("price")
	distributor = builder.SongsNamesMap.get(s).get("produced_by")
	if (Earnings.get(distributor)){
		price = Earnings.get(distributor) + price
	}
	Earnings.put(distributor, price)
}


def earnings_list = []
Earnings.each{k,v ->
	earnings_list.add(v)
}

def sort(list) {
	if (list.isEmpty()) return list
	anItem = list[0]
	def smallerItems = list.findAll{it < anItem}
	def equalItems = list.findAll{it == anItem}
	def largerItems = list.findAll{it > anItem}
	sort(largerItems) + equalItems + sort(smallerItems)
}


def sorted_earnings = sort(earnings_list)


StringWriter html_writer2 = new StringWriter()
def build2 = new MarkupBuilder(html_writer2)
build2.html{
head{
 style(type:"text/css", '''
    	table {
		    width:30%;
		}
		table, th, td {
		    border: 1px solid black;
		    border-collapse: collapse;
		}
		th, td {
		    padding: 5px;
		    text-align: left;
		}
		table#t01 tr:nth-child(even) {
		    background-color: #eee;
		}
		table#t01 tr:nth-child(odd) {
		   background-color:#fff;
		}
		table#t01 th	{
			border: 1px solid white;
		    background-color: black;
		    color: white;
		    text-align: center;
		}
    ''')  
}
body{
 h1("Earnings per Distributor")
 
		   table('id':"t01"){
			   tr{
		   
		   th("Distributors") 
		   th("Earnings")}
		
		   for (e in sorted_earnings){
			   Earnings.each{k,v ->
			   		if (v == e) {
						   tr{td(k)
							  td(v)}
			   		}
			   }
		   }
		  
	   }
 }

}
new File("distributors_earnings.html").write(html_writer2.toString())
