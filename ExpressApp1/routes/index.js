var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function (req, res) {
    res.render('index', { title: 'Express' });
});

//Start database
var MongoClient = require('mongodb').MongoClient;
var assert = require('assert');
var ObjectID = require('mongodb').ObjectID;
var url = 'mongodb://localhost:27017/local';

router.get('/Speech/:sentence', function (req, res) {

	//Process input
	var sentence = req.params.sentence;

	sentence = sentence.toLowerCase();
	var splitSentence = sentence.split("+");

	if (isQuestion(splitSentence)) {
		processedQuestion = processQuestion(splitSentence);
		//var result = queryDatabase(processedQuestion);
		var obj = [];
		
		var subject = "";

		var protoSubject = processedQuestion[2].toString();
		for (i = 0; i < protoSubject.length; i++) {
			if (protoSubject.charAt(i) === ",") {
				subject = subject.concat(" ");
			}
			else {
				subject = subject.concat(protoSubject.charAt(i));
			}
		}
		
		var verb = processedQuestion[1];

		//Conditions for the search are below. 
		//For now, I 'm not going to deal with the questionWord field, just find things that match subject, and verb..
		
		var prepositionList;
		if (processedQuestion[0] === "when") {
			prepositionList = [{ "preposition": "on" }, { "preposition": "at" }];
		}
		if (processedQuestion[0] === "where") {
			prepositionList = [{ "preposition": "at" }, { "preposition": "in" }]
		}
		
		//var test = [subject, verb, prepositionList];
		//res.send(test);

		
		var findSpeechData = function (db, callback) {
			var cursor = db.collection('SpeechData').find(
				{
					"verb": verb, "subject": subject,
					$or: prepositionList
				}
			);
			
			
			cursor.each(function (err, doc) {
				assert.equal(err, null);
				if (doc != null) {
					console.dir(doc);
					//var prepo = doc.preposition;
					obj.push(doc);
					obj[0].result = "ok";
				}
				else {
					callback();
				}
			});
		};
		
		MongoClient.connect(url, function (err, db) {
			assert.equal(null, err);
			
			
			findSpeechData(db, function () {
				res.send(obj[0]);
				db.close();

			});
		
		});
	}
	else {
		processedStatement = processStatement(splitSentence);
		addDatabaseEntry(processedStatement);
		
		var status = { "result": "ok" };

		res.send(status);
	}
});

router.get('/SpeechTest/:sentence', function (req, res) {
	var sen = req.params.sentence;
	res.send(sen);
});
module.exports = router;

function isQuestion(splitSentence) {
	var question;
	
	//List of question words.
	var questionWords = ["what", "when", "why", "how", "where", "who", "is"];
	
	//Check first word to identify if it's a question or a statement.
	var firstWord = splitSentence[0];
	if (questionWords.indexOf(firstWord) > -1) {
		question = true;
	}
	else {
		question = false;
	}
	return question;
}

function processQuestion(splitSentence) {
	var questionWord = splitSentence[0];
	//Naive assumptions below.
	var verb = splitSentence[1];
	var subject = splitSentence.slice(2);

	return [questionWord , verb , subject] 		
}

/*
function queryDatabase(processedQuestion) {
	var obj = [];
	
	//Conditions for the search are below. 
	//For now, I 'm not going to deal with the questionWord field, just find things that match subject, and verb..
	
	var prepositionList;
	if (processedQuestion[0] === "when") {
		prepositionList = [{ "preposition": "on" }, { "preposition": "at" }];
	}
	if (processedQuestion[0] === "where") {
		prepositionList = [{ "preposition": "at" }, { "preposition": "in" }]
	}

	var findSpeechData = function (db, callback) {
		var cursor = db.collection('SpeechData').find(
			{
				"verb": processedQuestion[1], "subject": processedQuestion[2],
				$or: prepositionList
			}
		);


		cursor.each(function (err, doc) {
			assert.equal(err, null);
			if (doc != null) {
				console.dir(doc);
				//var prepo = doc.preposition;
				obj.push(doc);
			}
			else {
				callback();
			}
		});
	};
	
	MongoClient.connect(url, function (err, db) {
		assert.equal(null, err);
		
		
		findSpeechData(db, function () {
			res.send(obj);
			db.close();

		});
		
	});

	//return obj;
}
*/

function processStatement(splitSentence) {
	//Currently assumes that the verb is "is"
	var indexOfVerb = splitSentence.indexOf("is");
	
	var protoSubject = splitSentence.slice(0, indexOfVerb);
	protoSubject = protoSubject.toString();
	var subject="";
	for (x = 0; x < protoSubject.length; x++) {
		if (protoSubject.charAt(x) === ',') {
			subject = subject.concat(' ');
		}
		else {
			subject = subject.concat(protoSubject.charAt(x));
		}
	}
	verb = splitSentence[indexOfVerb];
	
	//Assumes that the preposition immediately follows the verb.
	preposition = splitSentence[indexOfVerb + 1];

	protoObject = splitSentence.slice(indexOfVerb + 2);
	protoObject = protoObject.toString();
	object = "";
	for (x = 0; x < protoObject.length; x++) {
		if (protoObject.charAt(x) === ',') {
			object= object.concat(' ');
		}
		else {
			object = object.concat(protoObject.charAt(x));
		}
	}
	
	console.log("subject:" + subject + "object: " + object);
	
	return [subject, verb, preposition, object];
}

function addDatabaseEntry(processedStatement) {
	var insertDocument = function (db, callback) {
		db.collection('SpeechData').insertOne({
			"subject": processedStatement[0],
			"verb": verb,
			"preposition": preposition,
			"object": processedStatement[3]
		},
	function (err, result) {
			assert.equal(err, null);
			console.log("Inserted a document into the SpeechData collection.");
			callback();
		});
	};

	MongoClient.connect(url, function (err, db) {
		assert.equal(null, err);
		insertDocument(db, function () {
			db.close();
		});
	});
}