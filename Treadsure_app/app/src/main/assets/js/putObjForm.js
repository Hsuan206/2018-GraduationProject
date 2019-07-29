//let closeIcon=function(){};
//let okButton=function(){};
//div id="sendForm"
//title id="ob_title"
//conten id="ob_content"
//ob_type id="ob_type"
//carousel-inner id="objImg1" "objImg2" "objImg3"
//img src


let src=[];
src[0]='img/bulbasaur.jpg';
src[1]='img/geodude.jpg';
src[2]='img/poliwhirl.jpg';

//let user="FDZQcGP1RjbfLACovlORVyYRXMJ2";//////////要記得刪

let putObjForm='<div class="ui message" id="sendForm"> <i class="close icon" onclick="closeIcon()"></i> <div class="header" style="font-family:"微軟正黑體"; font-size:150%"> 你希望留下甚麼資訊?</div> <div class="object"> <form name="reg"></form> 標題： <input id="ob_title" name="ob_title" type="text" placeholder="請輸入10字以內" required maxlength="10" /><br/> 內容： <input id="ob_content" name="ob_title" type="text" placeholder="請輸入30字以內" /><br/> 類型： <select id="ob_type" name="ob_type" rel="--"> <option value="交友">交友</option> <optgroup> <option>交友</option> <option>揪團</option> <option>其他</option> </optgroup> </select><br/> <br/> <div id="select"> <div class="container-fluid"> <div class="row"> <div class="col-md-12"> <div class="carousel slide" id="carousel-19915" data-interval="0"> <div class="carousel-inner"> <div id="objImg1" class="carousel-item active"> <img class="d-block w-100" alt="Carousel Bootstrap First" src='+src[0]+' > </div> <div id="objImg2" class="carousel-item"> <img class="d-block w-100" alt="Carousel Bootstrap Second" src='+src[1]+' > </div> <div id="objImg3" class="carousel-item"> <img class="d-block w-100" alt="Carousel Bootstrap Third" src='+src[2]+' > </div> </div> <a class="carousel-control-prev" href="#carousel-19915" data-slide="prev"> <span class="carousel-control-prev-icon"></span> <span class="sr-only">Previous</span> </a> <a class="carousel-control-next" href="#carousel-19915" data-slide="next"> <span class="carousel-control-next-icon"></span> <span class="sr-only">Next</span> </a> </div> </div> </div> </div> </div> <br/> <button id="OK" onclick="okButton()">OK</button> <button id="cancel" onclick="closeIcon()">取消</button> <div> </div> </form> </div></div>';
document.getElementById("putObjForm").innerHTML=putObjForm;

let okButton = function () {
    

    let ob_title=document.getElementById("ob_title").value;
    if(ob_title==""){
        alert("請輸入標題");
        return;
    }
   let ob_content=document.getElementById("ob_content").value;
   if(ob_content==""){
        alert("請輸入內容");
        return;
    }
    let ob_type=document.getElementById("ob_type").value;


    let lat=document.getElementById("lat").value;
    let lon=document.getElementById("lon").value;
    var XY = gcs2Gauss(lon,lat);

    let obj = [];
    obj[0] = document.getElementById("objImg1").className;
    obj[1] = document.getElementById("objImg2").className;
    obj[2] = document.getElementById("objImg3").className;
    let obj_id = 0;

    for(let i=0;i<=2;i++){
        if(obj[i]=="carousel-item active"){
            obj_id=i+1;
        }
    }


    let oid = randomWord(false,16);
    
    firebase.database().ref('Object/'+oid).set({
        module: obj_id,
        Latitude: lat,
        Longitude : lon,
        content : ob_content,
        date: firebase.database.ServerValue.TIMESTAMP,
        title: ob_title,
        type: ob_type,
        user: userID,
        x :XY[0],
        y: XY[1]

      });
    console.log("創建成功");
    noneById("sendForm");
    init();
    
};

let closeIcon=function(){

    noneById("sendForm");
};