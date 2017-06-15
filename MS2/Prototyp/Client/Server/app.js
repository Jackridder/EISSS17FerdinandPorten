var express = require('express');
var bodyParser = require('body-parser');
var mongo = require('mongodb').MongoClient;
var jsonParser = bodyParser.json();
var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;
var url = 'mongodb://localhost:27017/test';
var app = express();

var accept = 0;

app.use(bodyParser.urlencoded({ extended: false }))
app.listen(3000,function(){
    console.log("Server running on 3000");
});

app.use(jsonParser);

app.get('/', function(req,res){

});

app.get('/deleteDB', function(req,res){
  delAll();
});

app.post('/insertAudio', function (req,res,next){
  var item = {
    aufnahme: req.body.aufnahme,
  }
  mongo.connect(url, function(err, db){
    if(err){
      console.log("Fehler");
      console.log(err);
    }
    else{
      db.collection('audioData').insertOne(item, function(err, result){
        console.log("Inserted Audio: " + item);
        db.close();
      });
    }
  });
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



app.get('/Empfehlung', function (req, res){
  //Nur für den Prototypen:
  getWeather('2913761');

  //--Normaler Code--
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


// Für zukünftiges: res.end(average <= 40 ? "0" : average > 40 && average <= 60 ? "1" : average > 60 ? "2" : "1");
