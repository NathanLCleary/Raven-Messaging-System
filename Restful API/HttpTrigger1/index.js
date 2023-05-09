var Connection = require('tedious').Connection;
var Request = require('tedious').Request
var TYPES = require('tedious').TYPES;

module.exports = function (context, req) {
    const name = (req.query.name || (req.body && req.body.name));
    var _fullData = [];

  
    try {
      const { username } = context.bindingData;
      
      var usernameText = username;

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

          request = new Request("SELECT * FROM RavenMessages where topic = '" + usernameText + "';", function(err) {
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
    
    


};