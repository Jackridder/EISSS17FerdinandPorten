var express = require('express');
var bodyParser = require('body-parser');
var mongo = require('mongodb').MongoClient;
var jsonParser = bodyParser.json();
var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
var url = 'mongodb://localhost:27017/test';
var app = express();
//XMLHTTPRequest URL und Api Keys
const weather_url = "http://api.openweathermap.org/data/2.5/weather?";
const googleurl = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=';
const apikey = "&APPID=09c7cedb04ff64e52b837e3f4e0ff6a0";
const googleapi = 'AIzaSyAPjItR1uYQuB2BkyYwyWsmTfdncL0z0o4';

//Für Lokalisierung
var params = {
  location: '51.022166,7.562483',
  radius: 20,
  type: ""
};
var personal = 0;

var keyWordHead = ["FAMILIE","PERSONAL","GERUCH","RIECHT","ROCH","ESSEN","GERICHT"];
var keyWordRating = ["GUT", "SCHLECHT", "NETT","UNFREUNDLICH","SCHRECKLICH","UNANGENEHM", "TOLL","LECKER", "OKAY"];
var currentloc = "";
var accept = 0;
var acceptKeyWords = 0;

app.use(bodyParser.urlencoded({ extended: false }))
app.listen(3000,function(){
    console.log("Server running on 3000");
});


app.use(jsonParser);

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

//Dient zur Ermittlung der Atmosphäre mit den gesammelten Daten.
app.get('/atmosphere', function(req,res){
  var resultArray = [];
  var countPersonal = 0;
  var places = [];
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      var cursor = db.collection('rating').find();
      cursor.forEach(function(doc,err){
        if(err){
          console.log("MongoDB kann auf userRatings nicht zugreifen");
          console.log(err);
        }
        else{
          resultArray.push(doc);
        }
      }, function() {
          var counter = 0;
          //Restaurants werden aus der DB geholt und in places gespeichert.
          resultArray.forEach(function(doc,err){
            counter = 0;
            for(var i = 0; i < 5; i++){
              counter++;
              if(doc.location == places[i]){
                counter--;
              }
            }
            if(counter == 5){
              places.push(doc.location);
            }
          });
          //Für jeden Standort werden Daten wie die Sprache, die Lautstärke und auch die gesprochene Sprache ermittelt
          places.forEach(function(place,err){
            var items = {
              placename: place,
              personalAll: 0,
              personalCount: 0,
              familieAll: 0,
              familieCount: 0,
              smellAll: 0,
              smellCount: 0,
              tasteAll: 0,
              tasteCount: 0,
              noiseAll: 0,
              noiseCount: 0,
              placelanguage: "",
            };
            //Benutzer Bewertungen werden mit einem Keywort Filter durchsucht. Werden Daten gefunden, werden diese in die Datenbank geschrieben
            resultArray.forEach(function(doc,err){
                if(place == doc.location){
                  var tempCount = items.personalAll;
                  items.personalAll += doc.hosp == "SCHRECKLICH" ? 5 : doc.hosp == "UNFREUNDLICH" ? 4 : doc.hosp == "OKAY" ? 3 : doc.hosp == "GUT" ? 2 : doc.hosp == "NETT" ? 1 : 0;
                  if(tempCount != items.personalAll){
                      items.personalCount += 1;
                  }
                  tempCount = items.familieAll;
                  items.familieAll += doc.fam == "SCHRECKLICH" ? 5 : doc.fam == "SCHLECHT" ? 4 : doc.fam == "OKAY" ? 3 : doc.fam == "GUT" ? 2 : doc.fam == "TOLL" ? 1 : 0;
                  if(tempCount != items.familieAll){
                      items.familieCount += 1;
                  }
                  tempCount = items.smellAll;
                  items.smellAll += doc.smell == "UNANGENEHM" ? 5 : doc.smell == "SCHLECHT" ? 4 : doc.smell == "NETT" ? 3 : doc.smell == "GUT" ? 2 : doc.smell == "LECKER" ? 1 : 0;
                  if(tempCount != items.smellAll){
                      items.smellCount += 1;
                  }
                  tempCount = items.tasteAll;
                  items.tasteAll += doc.taste == "SCHRECKLICH" ? 5 : doc.taste == "SCHLECHT" ? 4 : doc.taste == "OKAY" ? 3 : doc.taste == "GUT" ? 2 : doc.taste == "LECKER" ? 1 : 0;
                  if(tempCount != items.tasteAll){
                      items.tasteCount += 1;
                  }
                }
              });
              //Die Geräuschaufnahmen werden hier ermittelt
              var cursor = db.collection('userRatings').find();
              cursor.forEach(function(doc,err){
                if(err){
                  console.log("MongoDB kann auf userRatings nicht zugreifen");
                  console.log(err);
                }
                else{
                  if(place == doc.location){
                    var tempCount = items.noiseAll;
                    items.noiseAll += doc.noise;
                    if(tempCount != items.noiseAll){
                        items.noiseCount += 1;
                    }
                    items.placelanguage = doc.language;
                  }
                }
              }, function() {
                  db.collection('atmosphere').insertOne(items, function(err, result){
                    console.log("Inserted atmosphere on Place " + items.placename);
                    db.close();
                  });
                });
            });
          });
      };
    });
});

app.put('/audioData',function(req,res){
  //Kommentar muss rausgenommen werden für echte Werte!
  //params.location=req.body.latitude+","+req.body.longitude;
  //getOrt();
  //Beim Prototypen haben wir uns auf Restaurants in der Nähe beschränkt
  params.type = "restaurant";
  params.radius = 20;
  if(req.body.username == undefined){
    //Nur der Standort wird zurückgegeben
    if(getOrt(params.type) != undefined){
      console.log("Aktueller Standort:" + getOrt(params.type));
      res.end(getOrt(params.type));
    }
  }else{
    //Übermittelte Daten werden gespeichert und "verwaltet"
    if(currentloc!=""){
      var items = {
        username: req.body.username,
        noise: req.body.noise,
        location: currentloc,
        rating: req.body.rating,
        language: ""
      }
      mongo.connect(url, function(err, db){
        if(err){
          console.log("Fehler");
          console.log(err);
        }
        else{
          db.collection('userRatings').insertOne(items, function(err, result){
            console.log("Inserted rating for current Location: " + items.location);
            //Analysiert gegebene Keywords und weist entsprechend zu
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

//Benutzer wird registriert
app.put('/userData', function (req,res,next){
  if(req.body.email == undefined){
    var items = {
      username: req.body.username,
      password: req.body.password
    }
    //Ist der Benutzer registriert wird er angemeldet.
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
              //Überprüfung der Login Daten
              for (var i = 0; i < resultArray.length; i++){
                counter++;
                console.log(resultArray[i].username);
                if(resultArray[i].username == items.username && resultArray[i].password == items.password){
                  counter--;
                  console.log("Benutzer gefunden!");
                  accept = 1;
                  //Anmeldung erfolgreich
                  res.end(resultArray[i].username);
                }
              }
              //Anmeldung nicht erfolgreich
              if(counter == resultArray.length){
                console.log("Benutzer nicht gefunden");
                res.end("0");
                accept = 0;
              }
          });
        }
      });
  }
  else{
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
  }
});

//Benutzer hat sein Profil überarbeitet.
app.put('/userData/config', function (req,res,next){
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
          //Wird der Benutzer gefunden und es sind Daten vorhanden, werden diese Daten überschrieben
          for (var i = 0; i < resultArray.length; i++){
            counter++;
            if(resultArray[i].username == items.username){
              counter--;
              console.log("Benutzer gefunden!");
              var myquery = { username: items.username };
              var newvalues = { $set: { childs: items.childs, language: items.language } };
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
              //Hat der Benutzer noch kein Profil angelegt, wird ein neues angelegt
              console.log("Profil nicht gefunden");
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

app.put('/recommendation', function (req, res){
  //Testdaten wurden nur für die Restaurants in der Nähe angelegt (500m Umkreis von TH Mensa)
  //Ermittle Stadtname
  var empfehlung = [];
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function(){
    if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
      var data = JSON.parse(xmlhttp.responseText);
      //Ermittle Wetter
      var xmlhttp2 = new XMLHttpRequest();
      xmlhttp2.onreadystatechange = function(){
        if(xmlhttp2.readyState == 4 && xmlhttp2.status == 200){
          var data2 = JSON.parse(xmlhttp2.responseText);
          var weathertemp = Math.round(data2.main.temp-273.15);
          console.log("Wetter Temperatur: " + weathertemp);
          params.type="restaurant";
          params.radius=500;
          //Echte GPS Daten. Für die Vorführung werden die Daten der TH Köln Mensa benutzt
          //params.location=req.body.latitude+","+req.body.longitude;
          //Für mehr als nur restaurant: params.type=req.body.type;
          //Ermittle potentielle Orte in der Nähe
          var xmlhttp3 = new XMLHttpRequest();
          xmlhttp3.onreadystatechange = function(){
            if(xmlhttp3.readyState == 4 && xmlhttp3.status == 200){
              var data3 = JSON.parse(xmlhttp3.responseText);
              mongo.connect(url, function(err, db){
                if(err){
                  console.log("Fehler");
                  console.log(err);
                }
                else{
                  //Hole nötige Benutzerinformationen aus der DB
                  var userlang = "";
                  var userchild = 0;
                  var cursor = db.collection('userconfig').find();
                  cursor.forEach(function(doc,err){
                    if(err){
                      console.log("MongoDB kann auf userRatings nicht zugreifen");
                      console.log(err);
                    }
                    else{
                      if(doc.username == req.body.username){
                        userlang = req.body.language;
                        userchild = req.body.childs;
                      }
                    }
                  }, function() {
                    //Hole Daten aus der Atmosphäre Tabelle
                      var placeList = [];
                      var cursor2 = db.collection('atmosphere').find();
                      cursor2.forEach(function(doc,err){
                        if(err){
                          console.log("MongoDB kann auf userRatings nicht zugreifen");
                          console.log(err);
                        }
                        else{
                          //data3 = aktuelle Ortsliste
                          for(var i = 0; i < 5; i++){
                            //Ist einer der potentiellen Reiseziele/Restaurants in der DB, speichere den Ort in die placeListe
                            if(data3.results[i].name.search(doc.placename) >= 0 ){
                              console.log("Place added");
                              placeList.push(doc);
                            }
                          }
                        }
                      }, function() {
                          db.close();
                          var tempRank = [];
                          if(placeList.length > 0){
                            for(var i = 0; i < placeList.length; i++){
                              /*
                              Sprachen vergleichen
                            Familie nur relevant bei Kindern (Wenn Kinder dann Skala) (5 schlecht und 1 sehr gut)
                            Personal Skala erstellen (5 schlecht und 1 sehr gut)
                            Geruch Skala erstellen (5 schlecht und 1 sehr gut)
                            Essen Skala erstellen (5 schlecht und 1 sehr gut)
                            Geräusche Skala erstellen ( 5 schlecht und 1 sehr gut)
                            Wetter: Temperatur Abhängigkeit Skala (5 schlecht und 1 sehr gut) Wetter habe ich
                            */
                              //Ermittle den durchschnittlichen Wert aller Daten. Schulnoten ähnlich: schlecht = 5 und gut bis sehr gut = 1
                              var personalRank = placeList[i].personalAll/placeList[i].personalCount;
                              var familyRank = placeList[i].familieAll/placeList[i].familieCount;
                              //Hat der angemeldete Benutzer keine Kinder, dann wird das Familienranking vernachlässigt
                              if(userchild == 0){
                                familyRank = 0;
                              }
                              var smellRank = placeList[i].smellAll/placeList[i].smellCount;
                              var tasteRank = placeList[i].tasteAll/placeList[i].tasteCount;
                              var noiseRank = placeList[i].noiseAll/placeList[i].noiseCount;
                              //Spricht man am Ort eine der eigenen Sprachen gibt es die volle Punktzahl, sonst keine.
                              var checkLanguage = userlang.search(placeList[i].placelanguage) >= 0 ? 5 : 0;
                              //Gibt die Wetterschnittstelle bestimmte Werte zurück, werden diese unterschiedlich zwischengespeichert: kalt = 5, sommerliche Temperaturen = 1 und "heiß" = 3
                              var checkWeather = weathertemp < 10 ? 5 : weathertemp > 9 && weathertemp <= 25 ? 1 : 3;
                              var genRank = (personalRank + familyRank + smellRank + tasteRank + noiseRank + checkLanguage + checkWeather) / (familyRank == 0 ? 6 : 7);
                              tempRank.push(genRank);
                            }

                            //Sortierung der Liste nach "Ranking". Niedrig = Gut, Hoch = schlecht
                            for(var i = 0; i < placeList.length; i++){
                              if(i==0){
                                empfehlung.push(placeList[i]);
                              }
                              else{
                                empfehlung.push(placeList[i]);
                                for(var j = 0; j < empfehlung.length; j++){
                                  if(tempRank[i] < tempRank[j]){
                                    var temp = empfehlung[j];
                                    var ranktemp = tempRank[j];
                                    tempRank[j] = tempRank[i];
                                    tempRank[i] = ranktemp;
                                    empfehlung[j] = empfehlung[i];
                                    empfehlung[i] = temp;
                                  }
                                }
                              }

                            }
                            //Erstelle Empfehlung die an Client gesendet wird:
                            var empfehlungsstring = "";
                            for(var i = 0; i < empfehlung.length; i++){
                              if(i == 4){
                                empfehlungsstring += empfehlung[i].placename
                              }else{
                                empfehlungsstring += empfehlung[i].placename+",";
                              }
                            }
                            console.log("Empfehlung: " + empfehlungsstring);
                            res.end(empfehlungsstring);
                          }
                        });
                      });
                    };
                  });
            }
          }
          xmlhttp3.open("GET",googleurl + params.location + "&radius=" + params.radius + "&type="+params.type+"&key=" + googleapi, true);
          xmlhttp3.send();
        }
      }
      xmlhttp2.open("GET",weather_url + "q="+data.results[0].name + apikey, true);
      xmlhttp2.send();

      }
    }
  xmlhttp.open("GET",googleurl + params.location + "&radius=" + params.radius + "&type="+params.type+"&key=" + googleapi, true);
  xmlhttp.send();
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

//Hole den aktuellen Standort (nur die Stadt oder das aktuelle Restaurant)
function getOrt(type){
  console.log("type: " + type);
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = function(){
    if(xmlhttp.readyState == 4 && xmlhttp.status == 200){
      var data = JSON.parse(xmlhttp.responseText);
      if(data.results[0] == undefined){
        currentloc = "Sie befinden sich bei keinem Ort, der bewertet werden kann."
        return "Sie befinden sich bei keinem Ort, der bewertet werden kann.";
      }
      else{
        currentloc = data.results[0].name;
        return data.results[0].name;
        }
      }
    }
  xmlhttp.open("GET",googleurl + params.location + "&radius=" + params.radius + "&type="+params.type+"&key=" + googleapi, true);
  xmlhttp.send();
 }

function testDaten(){
  var user = {
    username: "test",
    password: "test",
    email: "test@test.de"
  };

//language ist nur für den Prototypen, später soll das Unternehmen die Sprache im CompyMark hinterlegen
  var userRatings = [{
    username: "test",
    rating: "War mit der Familie da und fand es toll.",
    location: "Mensa TH Köln",
    noise: 43,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 40,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Personal war super nett",
    language: "Deutsch, Chinesisch"
  },{
    username: "test",
    noise: 39,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Sehr gut für die Familie",
    language: "Deutsch, Chinesisch"
  },{
    username: "test",
    noise: 57,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Es roch sehr gut",
    language: "Deutsch, Chinesisch"
  },{
    username: "test",
    noise: 51,
    location: "Thairestaurant Bambusgarten Gummersbach",
    rating: "Das Essen dort war sehr lecker!",
    language: "Deutsch, Chinesisch"
  },{
    username: "test",
    noise: 80,
    location: "Dornseifers",
    rating: "Der Geruch war schlecht",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 70,
    location: "Dornseifers",
    rating: "Empfehlenswert für die Familie. Absolut gut.",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 49,
    location: "32 Süd",
    rating: "Es roch unangenehm",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 65,
    location: "32 Süd",
    rating: "Für die Familie nur absolut zu empfehlen. Sehr gut",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 42,
    location: "32 Süd",
    rating: "Sehr nettes Personal",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Essen war sehr gut",
    location: "32 Süd",
    noise: 50,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Essen war okay",
    location: "32 Süd",
    noise: 57,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Unheimlich unfreundliches Personal",
    location: "Mensa TH Köln",
    noise: 40,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Essen war unglaublich lecker.",
    location: "32 Süd",
    noise: 45,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Personal war super nett",
    location: "Dornseifers",
    noise: 57,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Essen war lecker.",
    location: "Dornseifers",
    noise: 49,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    rating: "Das Essen war unglaublich schlecht.",
    location: "Rhodos",
    noise: 50,
    language: "Deutsch, Griechisch"
  },{
    username: "Hans",
    rating: "Das Essen war okay.",
    location: "Rhodos",
    noise: 57,
    language: "Deutsch, Griechisch"
  },{
    username: "Max",
    rating: "Das Essen war unglaublich lecker.",
    location: "Mensa TH Köln",
    noise: 40,
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 58,
    location: "Mensa TH Köln",
    rating: "Es roch unangenehm",
    language: "Deutsch, Englisch"
  },{
    username: "test",
    noise: 62,
    location: "Rhodos",
    rating: "Es roch sehr gut",
    language: "Deutsch, Griechisch"
  },{
    username: "test",
    noise: 61,
    location: "Rhodos",
    rating: "Das angebotene Gericht war ganz gut",
    language: "Deutsch, Griechisch"
  },{
    username: "test",
    noise: 70,
    location: "Rhodos",
    rating: "Für die Famile ganz gut geeignet",
    language: "Deutsch, Griechisch"
  },{
    username: "test",
    noise: 78,
    location: "Rhodos",
    rating: "Das Personal war sehr nett",
    language: "Deutsch, Griechisch"
  }];

//Soll durch echte Daten ersetzt werden. Ist vorerst nicht im Prototypen
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

//Keywort Filter: Wird ein Keywort erkannt, wird es direkt identifziert und zwischengespeichert
function checkKeywords(request){
  var correctWords = 2;
  var rating = request.rating;
  var loc = request.location;
  rating = rating.toUpperCase();
  //Erster Begriff aus Array von keyWordHead. Für Familie, Personal, Geruch, etc.
  keyWordHead.forEach(function(doc,err){
    if(rating.search(doc) >= 0){
      correctWords--;
      //Zweiter Begriff aus Array von keyWordRating für schlecht, nett, lecker, freundlich, etc.
      keyWordRating.forEach(function(doc2,err){
        if(rating.search(doc2) >= 0){
          correctWords--;
          acceptKeyWords = 1;
          mongo.connect(url, function(err, db){
            //Bisher ist es nur möglich, ein "Head" und ein "Rating" zu vergleichen. Deshalb wird für jeden Head ein Rating gespeichert
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
              else if(doc == "PERSONAL"){
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
