var Connection = require('tedious').Connection;
var Request = require('tedious').Request
var TYPES = require('tedious').TYPES;

module.exports = function (context, req) {
    var _fullData = [];
    var id = null;
    var error = false;
    var lock = false;

    var usernameText = null;
    var passwordText = null;

    try {
        const { userId, newUsername, newPassword } = context.bindingData;

        id = userId;
        usernameText = newUsername;
        passwordText = newPassword;

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
            // UPDATE [dbo].[Users] SET username = 'Alfred Schmidt', password= 'Frankfurt' WHERE userID = 3; 
            var query = "UPDATE [dbo].[Users] SET username = '"+ usernameText +"', password = '" + passwordText +"' WHERE userID = " + id +";";
            console.log(query);
            var request = new Request(query, function(err) {
                if (err) {
                    context.log(err);
                    error = true;
                }
            });
            
            connection.execSql(request); 

            request.on('requestCompleted', function () {
                console.log("Error: " + error);
                //saveStatistic();
                context.log("_currentData");
                if(error == false){
                    console.log("Error: " + error);
                  context.res = {
                    // status: 200, /* Defaults to 200 */
                    body: "200"
                    };
                }else{
                    console.log("Error: " + error + " but is not false");
                    context.res = {
                        // status: 200, /* Defaults to 200 */
                        body: "405"
                        };
                } 
                
                context.done();
            });
              
        }
      }); 
}