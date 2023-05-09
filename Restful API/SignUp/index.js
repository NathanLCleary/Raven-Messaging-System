var Connection = require('tedious').Connection;
var Request = require('tedious').Request
var TYPES = require('tedious').TYPES;

module.exports = function (context, req) {
    var _fullData = [];
    id = null;

    var usernameText = null;
    var passwordText = null;

    try {
        const { username, password } = context.bindingData;

        var usernameText = username;
        var passwordText = password;

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
          
          request = new Request("INSERT INTO users (username, password) VALUES ('"+ usernameText+"', '" + passwordText +"');", function(err) {
            if (err) {
                context.log(err);}
            });
    
            request.on('requestCompleted', function () {
                //saveStatistic();
                context.log("_currentData");
                context.res = {
                    // status: 200, /* Defaults to 200 */
                    body: "complete"
                };
                context.done();
            });
            connection.execSql(request);   
        }
      }); 
}