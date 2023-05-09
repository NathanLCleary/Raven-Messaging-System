var Connection = require('tedious').Connection;
var Request = require('tedious').Request
var TYPES = require('tedious').TYPES;

module.exports = function (context, req) {
    const name = (req.query.name || (req.body && req.body.name));
    var _fullData = [];
    var returnValue = "";
    var id = "";

  
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

          request = new Request("SELECT * FROM Users where username = '" + usernameText + "';", function(err) {
            if (err) {
                context.log(err);}
            });
    
            request.on('row', function(columns) {
                var _currentData = {};
                
                id = columns[0].value;

                _currentData.id = columns[0].value;
                _currentData.userName = columns[1].value;
                _currentData.password = columns[2].value;
                context.log(_currentData);

                _fullData.push(_currentData);
            });
    
            request.on('requestCompleted', function () {
                //saveStatistic();
                context.log("_currentData");

                if(_fullData.length == 0){
                    // no user
                    returnValue = "400"
                }else{
                    currentValue = _fullData[0];
                    if(currentValue.password == passwordText){
                        // successful
                        returnValue = "200"
                    }else{
                        //wrong password
                        returnValue = "401"
                    }
                }

                context.res = {
                    // status: 200, /* Defaults to 200 */
                    body: returnValue + id
                };
                context.done();
            });
            connection.execSql(request);   
        }
      });

    context.log("Here");
    

    //context.done();
    
    


};