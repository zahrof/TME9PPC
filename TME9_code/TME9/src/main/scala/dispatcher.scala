
package upmc.akka.culto

import math._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.{Props, Actor, ActorRef, ActorSystem}

//Question 1
abstract class ObjetMusical

case class Note (pitch:Int, vol:Int, dur:Int) extends ObjetMusical
case class Chord (date:Int, notes:List[Note]) extends ObjetMusical
case class Chordseq (chords:List[Chord]) extends ObjetMusical


case class DatedNote (pitch:Int, vol:Int, dur:Int,date:Int) extends ObjetMusical
case class Voix (id:Int, notes:List[DatedNote]) extends ObjetMusical
////////////////////// DEBUT AJOUT

sealed trait VoiceMessage
case object ExtractorVoice extends VoiceMessage
case class Work(start: Int, nrOfElements: Int, cantate: Chordseq) extends VoiceMessage
case class Result(value: List[List[DatedNote]]) extends VoiceMessage
case class FinalCantate(content: List[Voix] ) extends VoiceMessage



class Worker extends Actor {
 def receive = {
  case Work(start, nrOfElements, cantate) => sender ! Result(transform(start, nrOfElements,cantate)) // perform the work
  }

 def transform(start: Int, nrOfElements: Int, cantate:Chordseq ): List[List[DatedNote]] = {
	cantate.synchronized{
		var result = List[List[DatedNote]]()
		var subList = cantate.chords.slice(start, start+nrOfElements -1)
		println("GIIIIIIIIIIIIII")
		println(cantate.chords(0).notes.size)
		val sz = cantate.chords(0).notes.size
		if(sz>=4){result = subList.map(x => DatedNote(x.notes(3).pitch,x.notes(3).vol,x.notes(3).dur, x.date)):: result }
		if(sz>=3){result = subList.map(x => DatedNote(x.notes(2).pitch,x.notes(2).vol,x.notes(2).dur, x.date)):: result }
		if(sz>=2){result = subList.map(x => DatedNote(x.notes(1).pitch,x.notes(1).vol,x.notes(1).dur, x.date)):: result }
		if(sz>=1){result = subList.map(x => DatedNote(x.notes(0).pitch,x.notes(0).vol,x.notes(0).dur, x.date)):: result }
		/*
		result = subList.map(x => DatedNote(x.notes(2).pitch,x.notes(2).vol,x.notes(2).dur, x.date)):: result
		result = subList.map(x => DatedNote(x.notes(1).pitch,x.notes(1).vol,x.notes(1).dur, x.date)):: result
		result = subList.map(x => DatedNote(x.notes(0).pitch,x.notes(0).vol,x.notes(0).dur, x.date)):: result
		*/		
		result	
}
	
 }
}

class Master(nrOfWorkers: Int, cantate: Chordseq ,listener: ActorRef) extends Actor {

  var transformedCantate: List[Voix] = Nil
  var workers: List[ActorRef] = Nil
  var rest: Int = cantate.chords.size % 5
  var nrOfMessages: Int = cantate.chords.size / 5
  var voix1: List[DatedNote]= Nil
  var voix2: List[DatedNote]= Nil
  var voix3: List[DatedNote]= Nil
  var voix4: List[DatedNote]= Nil 
  var nrOfResults: Int = 0 

  if (rest == 0 ){
	nrOfMessages = nrOfMessages-1
  }

  for (i <- (1 to 3).reverse)
    workers = context.actorOf(Props[Worker], name = "Worker" + i) :: workers

  def receive = {
  case ExtractorVoice =>
    for (i <- 0 until nrOfMessages) 
	if(i==nrOfMessages && rest != 0){
		workers (i % (nrOfWorkers - 1)) ! Work(i * 5, rest, cantate)
	}    
	else{  
		workers (i % (nrOfWorkers - 1)) ! Work(i * 5, 5, cantate)
	}
  case Result(value) => 
    
    voix1 = voix1 ++ value(0)
    voix2 = voix2 ++ value(1)
    voix3 = voix3 ++ value(2)
    voix4 = voix4 ++ value(3)

    nrOfResults = nrOfResults + 1
    

    if (nrOfResults == nrOfMessages) {
	transformedCantate = Voix ( 0, voix1):: Voix ( 1, voix2):: Voix ( 2, voix3):: Voix ( 3, voix4)::
				transformedCantate
      // Send the result to the listener
      listener ! FinalCantate(transformedCantate)
      // Stops this actor and all its supervised children
      context.stop(self)
    }
  }
}


////////////////////////// FIN AJOUT
/* MAIN */
object PlayCantate extends App {  
  val system = ActorSystem("VoicerSystem")

  val listener = system.actorOf(Props[Listener], name ="listener")

  //Question 1
  val cantate  = Chordseq ( List (Chord (0 , List (Note (50, 100, 4000), Note (65, 100, 4000), Note (69, 100, 4000), Note (62, 100, 4000)))
, Chord (4000 , List (Note (49, 100, 1000), Note (64, 100, 1000), Note (69, 100, 1000), Note (57, 100, 1000)))
, Chord (5000 , List (Note (50, 100, 1000), Note (62, 100, 1000), Note (65, 100, 1000), Note (57, 100, 1000)))
, Chord (6000 , List (Note (52, 100, 1000), Note (61, 100, 1000), Note (67, 100, 1000), Note (55, 100, 1000)))
, Chord (7000 , List (Note (53, 100, 500), Note (62, 100, 500), Note (69, 100, 1000), Note (62, 100, 1000)))
, Chord (7500 , List (Note (55, 100, 500), Note (64, 100, 500)))
, Chord (8000 , List (Note (57, 100, 1000), Note (65, 100, 1000), Note (65, 100, 1000), Note (62, 100, 1000)))
, Chord (9000 , List (Note (45, 100, 1000), Note (61, 100, 1000), Note (64, 100, 1000), Note (57, 100, 1000)))
, Chord (10000 , List (Note (50, 100, 1000), Note (57, 100, 1000), Note (62, 100, 1000), Note (53, 100, 1000)))
, Chord (11000 , List (Note (50, 100, 1000), Note (65, 100, 1000), Note (69, 100, 1000), Note (62, 100, 1000)))
, Chord (12000 , List (Note (48, 100, 1000), Note (64, 100, 1000), Note (69, 100, 1000), Note (57, 100, 1000)))
, Chord (13000 , List (Note (46, 100, 1000), Note (62, 100, 1000), Note (67, 100, 1000), Note (58, 100, 1000)))
, Chord (14000 , List (Note (45, 100, 500), Note (64, 100, 1000), Note (72, 100, 1000), Note (60, 100, 1000)))
, Chord (14500 , List (Note (48, 100, 500)))
, Chord (15000 , List (Note (53, 100, 1000), Note (65, 100, 1000), Note (69, 100, 1000), Note (60, 100, 1000)))
, Chord (16000 , List (Note (58, 100, 1000), Note (65, 100, 1000), Note (65, 100, 1000), Note (62, 100, 500)))
, Chord (16500 , List (Note (61, 100, 500)))
, Chord (17000 , List (Note (58, 100, 1000), Note (64, 100, 500), Note (67, 100, 1000), Note (62, 100, 1000)))
, Chord (17500 , List (Note (62, 100, 500)))
, Chord (18000 , List (Note (57, 100, 1000), Note (64, 100, 1000), Note (69, 100, 1000), Note (61, 100, 1000)))
, Chord (19000 , List (Note (57, 100, 500), Note (64, 100, 1000), Note (69, 100, 500), Note (60, 100, 1000)))
, Chord (19500 , List (Note (55, 100, 500), Note (71, 100, 500)))
, Chord (20000 , List (Note (53, 100, 1500), Note (69, 100, 1000), Note (72, 100, 1000), Note (60, 100, 500)))
, Chord (20500 , List (Note (57, 100, 500)))
, Chord (21000 , List (Note (67, 100, 1000), Note (74, 100, 500), Note (59, 100, 500)))
, Chord (21500 , List (Note (52, 100, 500), Note (76, 100, 500), Note (61, 100, 500)))
, Chord (22000 , List (Note (50, 100, 1000), Note (65, 100, 1000), Note (77, 100, 1000), Note (62, 100, 500)))
, Chord (22500 , List (Note (60, 100, 500)))
, Chord (23000 , List (Note (55, 100, 1000), Note (67, 100, 1000), Note (76, 100, 1000), Note (58, 100, 1000)))
, Chord (24000 , List (Note (56, 100, 1000), Note (65, 100, 500), Note (74, 100, 1000), Note (59, 100, 1000)))
, Chord (24500 , List (Note (64, 100, 500)))
, Chord (25000 , List (Note (57, 100, 1000), Note (64, 100, 1000), Note (73, 100, 1000), Note (57, 100, 1000)))
, Chord (26000 , List (Note (50, 100, 1000), Note (65, 100, 1000), Note (74, 100, 1000), Note (57, 100, 1000)))
, Chord (27000 , List (Note (55, 100, 1000), Note (65, 100, 1000), Note (74, 100, 1000), Note (59, 100, 1000)))
, Chord (28000 , List (Note (48, 100, 1000), Note (64, 100, 1000), Note (76, 100, 1000), Note (59, 100, 500)))
, Chord (28500 , List (Note (57, 100, 1000)))
, Chord (29000 , List (Note (47, 100, 1000), Note (71, 100, 1000), Note (74, 100, 1000)))
, Chord (29500 , List (Note (56, 100, 500)))
, Chord (30000 , List (Note (45, 100, 1000), Note (64, 100, 1000), Note (72, 100, 1000), Note (57, 100, 1000)))
, Chord (31000 , List (Note (52, 100, 1000), Note (64, 100, 500), Note (71, 100, 1000), Note (56, 100, 1000)))
, Chord (31500 , List (Note (62, 100, 500)))
, Chord (32000 , List (Note (53, 100, 1000), Note (60, 100, 500), Note (69, 100, 1000), Note (57, 100, 1000)))
, Chord (32500 , List (Note (62, 100, 500)))
, Chord (33000 , List (Note (52, 100, 1000), Note (64, 100, 1000), Note (68, 100, 1000), Note (59, 100, 1000)))
, Chord (34000 , List (Note (45, 100, 1000), Note (64, 100, 1000), Note (69, 100, 1000), Note (60, 100, 1000)))
, Chord (35000 , List (Note (55, 100, 500), Note (67, 100, 1000), Note (74, 100, 1000), Note (59, 100, 1000)))
, Chord (35500 , List (Note (53, 100, 500)))
, Chord (36000 , List (Note (52, 100, 1000), Note (67, 100, 1000), Note (72, 100, 1000), Note (60, 100, 1000)))
, Chord (37000 , List (Note (50, 100, 1000), Note (65, 100, 1000), Note (71, 100, 1000), Note (62, 100, 1000)))
, Chord (38000 , List (Note (48, 100, 1000), Note (67, 100, 1000), Note (72, 100, 1000), Note (64, 100, 1000)))
, Chord (39000 , List (Note (50, 100, 500), Note (65, 100, 1000), Note (69, 100, 1000), Note (62, 100, 1000)))
, Chord (39500 , List (Note (48, 100, 500)))
, Chord (40000 , List (Note (46, 100, 1000), Note (65, 100, 1000), Note (69, 100, 1000), Note (62, 100, 500)))
, Chord (40500 , List (Note (60, 100, 500)))
, Chord (41000 , List (Note (48, 100, 1000), Note (64, 100, 1000), Note (67, 100, 1000), Note (58, 100, 1000)))
, Chord (42000 , List (Note (53, 100, 1000), Note (60, 100, 1000), Note (65, 100, 1000), Note (57, 100, 1000)))
, Chord (43000 , List (Note (54, 100, 1000), Note (62, 100, 1000), Note (69, 100, 1000), Note (57, 100, 1000)))
, Chord (44000 , List (Note (55, 100, 1000), Note (62, 100, 1000), Note (70, 100, 1000), Note (55, 100, 500)))
, Chord (44500 , List (Note (53, 100, 500)))
, Chord (45000 , List (Note (49, 100, 1000), Note (64, 100, 1000), Note (69, 100, 500), Note (52, 100, 1000)))
, Chord (45500 , List (Note (67, 100, 500)))
, Chord (46000 , List (Note (50, 100, 500), Note (62, 100, 1000), Note (65, 100, 1000), Note (57, 100, 1000)))
, Chord (46500 , List (Note (48, 100, 500)))
, Chord (47000 , List (Note (46, 100, 500), Note (62, 100, 1000), Note (67, 100, 1000), Note (55, 100, 500)))
, Chord (47500 , List (Note (45, 100, 500), Note (57, 100, 500)))
, Chord (48000 , List (Note (44, 100, 1333), Note (62, 100, 1333), Note (65, 100, 1333), Note (59, 100, 1333)))
, Chord (49333 , List (Note (45, 100, 1333), Note (61, 100, 1333), Note (64, 100, 1333), Note (52, 100, 667)))
, Chord (50000 , List (Note (53, 100, 333)))
, Chord (50333 , List (Note (55, 100, 333)))
, Chord (50667 , List (Note (50, 100, 1333), Note (57, 100, 1333), Note (62, 100, 1333), Note (54, 100, 1333)))
))

  val master = system.actorOf(
		Props(new Master(3, cantate,listener)), name ="master")
 
  
//val remote = system.actorSelection("akka.tcp://Player@127.0.0.1:6000/user/PlayerActor")
//val remote = system.actorSelection("akka.tcp://Player@192.168.1.41:6000/user/PlayerActor")
  //To test your cantate send this msg
  //remote ! cantate
  master ! ExtractorVoice

 //End Question 1


////////////////////QUESTION 2


}

class Listener extends Actor {
  val remote = context.system.actorSelection("akka.tcp://Player@127.0.0.1:6000/user/PlayerActor")
  def receive = {
    case FinalCantate(cantate) =>
      remote ! cantate(0)
      remote ! cantate(1)
      remote ! cantate(2)
      remote ! cantate(3)
  }
}
