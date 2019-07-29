//專案初始化，Authentication後右方有個網路設定
var config = {
  apiKey: "AIzaSyCzaYpvOmLYeERBRCcO8M_e4ryAE57nLB8",
  authDomain: "fir-1-2b746.firebaseapp.com",
  databaseURL: "https://fir-1-2b746.firebaseio.com",
  projectId: "fir-1-2b746",
  storageBucket: "fir-1-2b746.appspot.com",
  messagingSenderId: "772658594851"
};
firebase.initializeApp(config);

var id;
        function createOb() {
            var title = document.getElementById("ob_title").value;
            var content = document.getElementById("ob_content").value;
            var type = document.getElementById("ob_type").value; 
            id = randomWord(false,10);
            var mObjectDatabase = firebase.database()
            mObjectDatabase.ref('Object/XXXX').set({'title':name,
                            'content':content,
                            'type':type
            });


        }
        function randomWord(randomFlag, min, max){
            var str = "",
                range = min,
                arr = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
        
            // 随机产生
            if(randomFlag){
                range = Math.round(Math.random() * (max-min)) + min;
            }
            for(var i=0; i<range; i++){
                pos = Math.round(Math.random() * (arr.length-1));
                str += arr[pos];
            }
            return str;
        }
        /*
        function currentObject(msg) {
            var test = document.getElementById("currentObject");
            test.innerHTML = msg;
            objId = msg;
            var objectRef = firebase.database().ref().child('Object').orderByKey();
            objectRef.on('value', function(snapshot) {
                snapshot.forEach(function(childSnapshot) {                
            
                    var node = document.createElement('div');
                    node.setAttribute('onclick','objectId(this)');
                    node.setAttribute('id',childSnapshot.key);
                    let textnode = document.createTextNode(childSnapshot.key);
                    node.appendChild(textnode);
                    document.body.appendChild(node);
                   // document.write("<div onclick='objectId(this)' id='"+childSnapshot.key+"'>"+childSnapshot.key+"</div>");
                   
                      // var test = document.getElementById(childSnapshot.key);
                    //test.addEventListener("click",objectId(childSnapshot.key),false);
                });
                
            });

        }*/