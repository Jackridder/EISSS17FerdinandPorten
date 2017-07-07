var express = require('express');
var bodyParser = require('body-parser');
var mongo = require('mongodb').MongoClient;
var jsonParser = bodyParser.json();
var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
var url = 'mongodb://localhost:27017/test';
var app = express();
var googleapi = 'AIzaSyAPjItR1uYQuB2BkyYwyWsmTfdncL0z0o4';
var params = {
  location: '51.022166,7.562483',
  //lat: 51.0217676, lng: 7.562717900000001
  //Mensa: '51.022166,7.562483'
  //Bei uns: '51.02609198,7.55402856'
  //China Restaurant: 51.025729, 7.560473
  radius: 20,
  type: ""
};

var keyWordHead = ["FAMILIE","PERSONAL","GASTFREUNDLICH","GERUCH","RIECHT","ROCH","ESSEN","GERICHT"];
var keyWordRating = ["GUT", "SCHLECHT", "NETT","UNFREUNDLICH","SCHRECKLICH","UNANGENEHM", "TOLL","LECKER", "OKAY"];
var currentloc = "";
var accept = 0;
var acceptKeyWords = 0;

app.use(bodyParser.urlencoded({ extended: false }))
app.listen(3000,function(){
    console.log("Server running on 3000");
});


app.use(jsonParser);

app.get('/ort', function(req,res){
  params.radius = 500;
  console.log(getOrt()+"");
});

app.get('/testDaten', function(req,res){
  console.log("Datenbank gelöscht");
  delAll();
  testDaten();
  console.log("Testdaten eingefügt");
});

app.get('/testRatings', function(req,res){
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      var cursor = db.collection('userRatings').find();
      cursor.forEach(function(doc,err){
        if(err){
          console.log("MongoDB kann auf userRatings nicht zugreifen");
          console.log(err);
        }
        else{
          checkKeywords(doc);
        }
      }, function() {
          db.close();
      });
    }
  });
});

app.post('/audioData',function(req,res){
  //Kommentar muss rausgenommen werden für echte Werte!
  //params.location=req.body.latitude+","+req.body.longitude;
  //getOrt();
  if(req.body.username == undefined){
    if(getOrt() != undefined){
      console.log("Aktueller Standort:" + getOrt());
      res.end(getOrt()+"");
    }
  }else{
    if(currentloc!=""){
      var items = {
        user: req.body.username,
        noise: req.body.noise,
        location: currentloc,
        rating: req.body.rating
      }
      mongo.connect(url, function(err, db){
        if(err){
          console.log("Fehler");
          console.log(err);
        }
        else{
          db.collection('audioData').insertOne(items, function(err, result){
            console.log("Inserted rating for current Location: " + items.location);
            checkKeywords(items);
            db.close();
          });
        }
      });

    }
    else{
      console.log("Kein aktueller Standort");
    }
  }
  if(currentloc!=""){
    res.end(currentloc);
  }
});

app.post('/userData', function (req,res,next){
  var items = {
    username: req.body.username,
    password: req.body.password,
    email: req.body.email
  }
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      db.collection('user').insertOne(items, function(err, result){
        console.log("Inserted user: " + items.username);
        db.close();
      });
    }
  });
});

app.post('/userData/config', function (req,res,next){
  var items = {
    username: req.body.username,
    language: req.body.language,
    childs: req.body.children
  }
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      var counter = 0;
      var resultArray = [];
      var cursor = db.collection('userconfig').find();
      cursor.forEach(function(doc,err){
        if(err){
          console.log(err);
        }
        else{
          resultArray.push(doc);
        }
        }, function() {
          for (var i = 0; i < resultArray.length; i++){
            counter++;
            if(resultArray[i].username == items.username){
              counter--;
              console.log("Benutzer gefunden!");
              var myquery = { username: items.username };
              var newvalues = { $set: { childs: items.childs, language: items.language } };
              //var newvalues = { childs: items.childs, language: items.language };
              db.collection("userconfig").updateOne(myquery, newvalues, function(err, res) {
                if (err){
                  console.log(err);
                }
                else{
                  console.log("1 record updated");
                }
              });
            }
            if(counter == resultArray.length){
              console.log("Benutzer nicht gefunden");
              db.collection('userconfig').insertOne(items, function(err, result){
                console.log("Inserted user: " + items.username);
              });
            }
          }
          db.close();
        });
      }
    });
  });

app.post('/login', function(req,res,next){
  var items = {
    username: req.body.username,
    password: req.body.password
  }
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler beim Herstellen einer Verbindung zu MongoDB");
      console.log(err);
    }
    else{
      var resultArray = [];
      var cursor = db.collection('user').find();
      cursor.forEach(function(doc,err){
        if(err){
          console.log("MongoDB kann auf user nicht zugreifen");
          console.log(err);
        }
        else{
            resultArray.push(doc);
          }
        }, function() {
            db.close();
            var counter = 0;
            for (var i = 0; i < resultArray.length; i++){
              counter++;
              console.log(resultArray[i].username);
              if(resultArray[i].username == items.username && resultArray[i].password == items.password){
                counter--;
                console.log("Benutzer gefunden!");
                accept = 1;
                res.end(resultArray[i].username);
              }
            }
            if(counter == resultArray.length){
              console.log("Benutzer nicht gefunden");
              res.end("0");
              accept = 0;
            }
        });
      }
    });
  });



app.post('/Empfehlung', function (req, res){
  params.radius = 500;
  //params.location=req.body.latitude+","+req.body.longitude;
  params.type = req.body.type;
  if(req.body.username == undefined){
    getOrt("restaurant");
    //getOrt(params.type);
  }
  else{

  }


  //Nur für den Prototypen:
  //getWeather('2913761');

  //--Normaler Code--
  /*
  var weatherrecommend = "";
  mongo.connect(url, function(err,db){
    var resultArray = [];
    var cursor = db.collection('weather').find();
    cursor.forEach(function(doc,err){
      resultArray.push(doc);
    }, function() {
      db.close();
      var temp = Math.round(resultArray[resultArray.length-1].temp-273.15);
      console.log("Temperatur: "+ temp);
      weatherrecommend = temp <= 10 ? "0" : temp > 10 && temp <= 20 ? "1" : "2";
      console.log("Wetter Empfehlung: " + weatherrecommend);
    });
  });

  mongo.connect(url, function(err,db){
    var resultArray = [];
    var cursor = db.collection('user').find();
    cursor.forEach(function(doc,err){
      resultArray.push(doc);
    }, function() {
      db.close();
      var tempVol = resultArray[resultArray.length-1].volume;
      var tempWea = resultArray[resultArray.length-1].weather;
      userVol = tempVol == "loud" ? 2 : tempVol == "roomvolume" ? 1 : 0;
      userWea = tempWea == "warm" ? 2 : tempWea == "spring" ? 1 : 0;
      console.log("Temp: "+weatherrecommend);
      console.log("BenutzerAngabe Wetter: " + userWea);
      console.log("BenutzerAngabe Lautstärke: " + userVol);
    });
  });
  res.end(tempNoise == 0 && weatherrecommend == userWea ? "Der Ort wird Ihnen empfohlen" : "Der Ort entspricht nicht Ihren Kriterien");
*/
});

function delAll(){
  mongo.connect(url, function(err,db){
    if(err){
        console.log("Fehler");
        console.log(err);
    }else{
      //Drop Complete Database
      db.dropDatabase();
    }
  });
}

function getWeather(location){
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function(){
    if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
      var data = JSON.parse(xmlhttp.responseText);
      weather.temp = data.main.temp;
      mongo.connect(url, function(err, db){
        if(err){
          console.log("Fehler");
          console.log(err);
        }
        else{
          db.collection('weather').insertOne(weather, function(err, result){
            console.log("Inserted Wetter: " + weather.temp);
            db.close();
          });
        }
      });
    }
  }
  xmlhttp.open("GET",weather_url + "id=" + location + apikey, true);
  xmlhttp.send();
}

//Testzwecke
function getNoise(volume){
  var resultArray = [];
  var noise = volume == 2 ? 100 : volume == 1 ? roomVolume : lowVolume;
  mongo.connect(url, function(err,db){
    var cursor = db.collection('audioData').find();
    cursor.forEach(function(doc,err){
      resultArray.push(doc);
    }, function() {
      db.close();
      average = 0;
      var totalAverage = 0;
      var difference = 0;
      var coefficient = 0;
      for(var i = 0; i < resultArray.length; i++){
        totalAverage += Math.floor(resultArray[i].aufnahme);
        if(Math.floor(resultArray[i].aufnahme) <= noise){
          average += Math.floor(resultArray[i].aufnahme);
          difference += 1.0;
        }
      }
      totalAverage = totalAverage/resultArray.length;
      if(totalAverage > noise-Math.floor(noise*0.05)){
        average = 0;
        difference = 0;
        for(var i = 0; i < resultArray.length; i++){
          if(Math.floor(resultArray[i].aufnahme) <= noise+(totalAverage-noise)){
            average += Math.floor(resultArray[i].aufnahme);
            difference += 1;
          }
          if(Math.floor(resultArray[i].aufnahme) > totalAverage){
            coefficient += 1;
          }
        }
        if(difference/coefficient <= 0.2){
          tempNoise = totalAverage <= noise+(totalAverage-noise) ? 0 : 1;
          return(totalAverage <= noise+(totalAverage-noise) ? "0" : "1");
        }
        else{
          average = average/difference;
          tempNoise = average <=(noise+(totalAverage-noise)) ? 0 : 1;
          return(average <= (noise+(totalAverage-noise)) ? "0" : "1");
        }
      }
      else{
        average = average/difference;
        tempNoise = average <= noise ? 0 : 1;
        return(average <= noise ? "0" : "1");
      }
    });
  });
}

function getOrt(type){
  var items = [];
  console.log("type: " + type);
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function(){
    if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
      var data = JSON.parse(xmlhttp.responseText);
      /*
      for(var i = 0; i < data.results.length; i++){
        console.log(data.results[i].name);
        console.log(data.results[i].geometry.location);
        for(var j = 0; j < data.results[i].types.length; j++){
          if(data.results[i].types[j] == 'restaurant' || data.results[i].types[j] == 'food'){
            var check = 0;
            for(var x = 0; x < items.length; x++){
              if(items[x].name == data.results[i].name){
                check++;
              }
            }
            if(check == 0){
              items.push({type:'restaurant', name: data.results[i].name});
            }
          }
        }


      }//
      //console.log(items);
      //console.log(data.results);
      */
      if(data.results[0] == undefined){
        currentloc = "Sie befinden sich bei keinem Ort, der bewertet werden kann."
        return "Sie befinden sich bei keinem Ort, der bewertet werden kann.";

      }
      else{
        for(var i = 0; i < data.results.length; i++){
          console.log("Ort: " + data.results[i].name);
        }
        //console.log(data.results[0].name);
        currentloc = data.results[0].name;
        return data.results[0].name;
      }
    }
  }

  var googleurl = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=';
  xmlhttp.open("GET",googleurl + params.location + "&radius=" + params.radius + "&type="+type+"&key=" + googleapi, true);
  //xmlhttp.open("GET",googleurl + params.location + "&rankby=distance" + "&type=restaurant&key=" + googleapi, true);
  //xmlhttp.open("GET",googleurl + params.location + "&rankby=distance" + "&key=" + googleapi, true);
  xmlhttp.send();
}


function testDaten(){
  var user = {
    username: "test",
    password: "test",
    email: "test@test.de"
  };
  /*
  Ort: Rhodos Hab
Ort: 32 Süd Hab
Ort: Mensa TH Köln Hab
Ort: Dornseifers
Ort: Thairestaurant Bambusgarten Gummersbach
*/

  var userRatings = [{
    username: "test",
    rating: "War mit der Familie da und fand es toll.",
    location: "Mensa TH Köln",
    noise: 43
  },{
    username: "test",
    noise: 40,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Personal war super nett"
  },{
    username: "test",
    noise: 39,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Sehr gut für die Familie"
  },{
    username: "test",
    noise: 57,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Es roch sehr gut"
  },{
    username: "test",
    noise: 51,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Das Essen dort war sehr lecker!"
  },{
    username: "test",
    noise: 80,
    location: "Dornseifers",
    rating: "Der Geruch war schlecht"
  },{
    username: "test",
    noise: 70,
    location: "Dornseifers",
    rating: "Empfehlenswert für die Familie. Absolut gut."
  },{
    username: "test",
    noise: 49,
    location: "32 Süd",
    rating: "Es roch unangenehm"
  },{
    username: "test",
    noise: 65,
    location: "32 Süd",
    rating: "Für die Familie nur absolut zu empfehlen. Sehr gut"
  },{
    username: "test",
    noise: 42,
    location: "32 Süd",
    rating: "Sehr nettes Personal"
  },{
    username: "test",
    rating: "Das Essen war sehr gut",
    location: "32 Süd",
    noise: 50
  },{
    username: "test",
    rating: "Das Essen war okay",
    location: "32 Süd",
    noise: 57
  },{
    username: "test",
    rating: "Unheimlich unfreundliches Personal",
    location: "Mensa TH Köln",
    noise: 40
  },{
    username: "test",
    rating: "Das Essen war unglaublich lecker.",
    location: "32 Süd",
    noise: 45
  },{
    username: "test",
    rating: "Das Personal war super nett",
    location: "Dornseifers",
    noise: 57
  },{
    username: "test",
    rating: "Das Essen war lecker.",
    location: "Dornseifers",
    noise: 49
  },{
    username: "test",
    rating: "Das Essen war unglaublich schlecht.",
    location: "Rhodos",
    noise: 50
  },{
    username: "Hans",
    rating: "Das Essen war okay.",
    location: "Rhodos",
    noise: 57
  },{
    username: "Max",
    rating: "Das Essen war unglaublich lecker.",
    location: "Mensa TH Köln",
    noise: 40
  },{
    username: "test",
    noise: 58,
    location: "Mensa TH Köln",
    rating: "Es roch unangenehm"
  },{
    username: "test",
    noise: 62,
    location: "Rhodos",
    rating: "Es roch sehr gut"
  },{
    username: "test",
    noise: 61,
    location: "Rhodos",
    rating: "Das angebotene Gericht war ganz gut"
  },{
    username: "test",
    noise: 70,
    location: "Rhodos",
    rating: "Für die Famile ganz gut geeignet"
  },{
    username: "test",
    noise: 78,
    location: "Rhodos",
    rating: "Das Personal war sehr nett"
  }];

  var walkingroutes = [{
    location: "Gummersbach",
    number: 15
  },{
    location: "Köln",
    number: 47
  },{
    location: "München",
    number: 60
  },{
    location: "Siegen",
    number: 12
  },{
    location: "Koblenz",
    number: 23
  },{
    location: "Leverkusen",
    number: 11
  },{
    location: "Dieringhausen",
    number: 3
  },{
    location: "Oberhausen",
    number: 17
  },{
    location: "Düsseldorf",
    number: 43
  }];
  var config = {
    username: "test",
    language: "Deutsch",
    childs: "0"
  }
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      walkingroutes.forEach(function(doc,err){
      db.collection('walkingroutes').insertOne(doc, function(err, result){
          console.log("Inserted walkingroutes: " + doc.location);
          db.close();
        });
      });
      userRatings.forEach(function(doc,err){
        db.collection('userRatings').insertOne(doc, function(err, result){
          console.log("Inserted userRatings: " + doc.rating);
          db.close();
        });
      });
      db.collection('user').insertOne(user, function(err, result){
          console.log("Inserted user: " + user.username);
          db.close();
      });
      db.collection('userconfig').insertOne(config, function(err, result){
          console.log("Inserted userconfig: " + config.language);
          db.close();
      });
    }
  });
}

function checkKeywords(request){
  var correctWords = 2;
  var rating = request.rating;
  var loc = request.location;
  rating = rating.toUpperCase();
  keyWordHead.forEach(function(doc,err){
    if(rating.search(doc) >= 0){
      //console.log("Erster Begriff gefunden: " + doc);
      correctWords--;
      keyWordRating.forEach(function(doc2,err){
        if(rating.search(doc2) >= 0){
          //console.log("Zweiter Begriff gefunden: " + doc2);
          correctWords--;
          acceptKeyWords = 1;
          mongo.connect(url, function(err, db){
            if(err){
              console.log("Fehler");
              console.log(err);
            }
            else{
              var items = {};
              if(doc == "FAMILIE"){
                items = {
                  location: loc,
                  fam: doc2,
                  smell: "",
                  hosp: "",
                  taste: ""
                }
              }
              else if(doc == "PERSONAL" || doc == "GASTFREUNDLICH"){
                  items = {
                    location: loc,
                    fam: "",
                    smell: "",
                    hosp: doc2,
                    taste: ""
                  }
                }
                else if(doc == "GERUCH" || doc == "RIECHT" || doc == "ROCH"){
                    items = {
                      location: loc,
                      fam: "",
                      smell: doc2,
                      hosp: "",
                      taste: ""
                    }
                  }
                  else{
                    items = {
                      location: loc,
                      fam: "",
                      smell: "",
                      hosp: "",
                      taste: doc2
                    }
                  }
                  db.collection('rating').insertOne(items, function(err, result){
                      console.log("Inserted rating: " + doc);
                      db.close();
                    });
              }
            });
        }
      });
    }
  });
}
  //return 1;
  /*for(var i = 0; i < keyWordHead.length; i++){
    console.log("keyWordSearch: " + keyWordHead[i]);
    if(rating.search(keyWordHead[i]) >= 0){
      console.log("Erster Begriff gefunden: " + keyWordHead[i]);
      correctWords++;
      for(var j = 0; j < keyWordRating.length;i++){
        //console.log("KeyWordSearch: " + keyWordRating[i]);
        if(rating.search(keyWordRating[j]) >= 0){
          console.log("Zweiter Begriff gefunden: " + keyWordRating[i]);
          correctWords++;
        }
      }
    }
  }
  */

// Für zukünftiges: res.end(average <= 40 ? "0" : average > 40 && average <= 60 ? "1" : average > 60 ? "2" : "1");
//Metrik Atmosphäre
