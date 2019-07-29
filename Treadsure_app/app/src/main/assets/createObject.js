var user;

//創建物件
function createObject() {
    // 存使用者ID

  user = document.getElementById("currentUser");
  user.innerHTML = userID;

  // 資訊
  var title = document.getElementById("ob_title").value;
  var content = document.getElementById("ob_content").value;
  var type = document.getElementById("ob_type").value; 
  //此物件ID日後設給物件就好
  var id = objIdrandom();
  var objectRef = firebase.database().ref().child('Object');
  /*objectRef.on('value',function(snapshot) { 
      var count = snapshot.numChildren();
      count = count + 1;
      var object = new Array(title,content,type,count.toString());
      window.ExtObj_createObject.responseResult(object);
  });*/

  //getobjid();
  var model = document.getElementById("model_type");
  var index = model.selectedIndex; 
  var model_id = model.options[index].getAttribute('id')

  console.log("模型ID : "+model_id);



  var object = new Array(title,content,type,id,model_id);
  window.ExtObj_createObject.responseResult(object);


}

var characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
var passwordlength=16;
function objIdrandom() {
            
  var password = "";
  var n = 0;
  var randomnumber = 0;
  while( n < passwordlength ) {
  n ++;
  randomnumber = Math.floor(characters.length*Math.random());
  password += characters.substring(randomnumber,randomnumber + 1);
  }
  return password;
}