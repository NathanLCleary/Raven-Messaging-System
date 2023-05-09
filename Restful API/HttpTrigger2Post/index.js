var Connection = require('tedious').Connection;
var Request = require('tedious').Request
var TYPES = require('tedious').TYPES;

module.exports = function (context, req) {
    var _fullData = [];
    id = null;


    var date = new Date();
	  var current_date = date.getFullYear()+"-"+(date.getMonth()+1)+"-"+ date.getDate()
     +" " + date.getHours()+":"+date.getMinutes()+":"+ date.getSeconds();


    var topicText = null;
    var messageText = null;

    try {
        const { topic, message } = context.bindingData;
    
        // var query = new azure.TableQuery()
        //   .where("PartitionKey eq ?", blog)
        //   .and("RowKey eq ?", id.toString());
    
        //const result = queryEntities("Posts", query);
    
        // context.res = {
        //   body: result,
        // };

        var topicText = topic;
        var messageText = message;

        _fullData.push(id);
        _fullData.push(current_date);
        _fullData.push(topic);
        _fullData.push(message);

      } catch (error) {
        context.res = {
          status: 500,
          body: error.message
        }
    }
    var config = {
        userName: '',
        password: '',
        server: '',
        options: {encrypt: true, database: ''}
    };

    var connection = new Connection(config);

    connection.on("connect", function (err) {
        if(err) {
          console.log('Error: ', err)
        } else {
          console.log("Successful connection");
          //getPerformance();
          
          request = new Request("INSERT INTO RavenMessages (time,topic,message) VALUES ('"+ current_date +"', '"+ topicText+"', '" + messageText +"');", function(err) {
            if (err) {
                context.log(err);}
            });
    
            request.on('row', function(columns) {
                var _currentData = {};
                _currentData.id = columns[0].value;
                _currentData.date = columns[1].value;
                _currentData.topic = columns[2].value;
                _currentData.message = columns[3].value;
                context.log(_currentData);

                _fullData.push(_currentData);
            });
    
            request.on('requestCompleted', function () {
                //saveStatistic();
                context.log("_currentData");
                context.res = {
                    // status: 200, /* Defaults to 200 */
                    body: _fullData
                };
                context.done();
            });
            connection.execSql(request);   
        }
      });


    context.log("Here");
    

    //context.done();
    
    


    // const name = (req.query.name || (req.body && req.body.name));
    // const responseMessage = name
    //     ? "Hello, " + name + ". This HTTP triggered function executed successfully."
    //     : "This HTTP triggered function executed successfully. Pass a name in the query string or in the request body for a personalized response.";



    // context.res = {
    //     // status: 200, /* Defaults to 200 */
    //     body: responseMessage
    // };
}