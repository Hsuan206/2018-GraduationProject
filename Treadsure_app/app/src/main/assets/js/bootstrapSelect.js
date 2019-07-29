let src=[];
src[0]='img/bulbasaur.jpg';
src[1]='img/geodude.jpg';
src[2]='img/poliwhirl.jpg';
let selectf=[];
selectf[0]='';
selectf[1]='';
selectf[2]='';

var bootstrapSelect='<div id="select"> <div class="container-fluid"> <div class="row"> <div class="col-md-12"> <div class="carousel slide" id="carousel-19915"> <ol class="carousel-indicators"> <li data-slide-to="0" data-target="#carousel-19915" class="active"> </li> <li data-slide-to="1" data-target="#carousel-19915"> </li> <li data-slide-to="2" data-target="#carousel-19915"> </ol> <div class="carousel-inner"> <div class="carousel-item active"> <img class="d-block w-100" alt="Carousel Bootstrap First" src='+src[0]+' onclick='+selectf[0]+'> </div> <div class="carousel-item"> <img class="d-block w-100" alt="Carousel Bootstrap Second" src='+src[1]+' onclick='+selectf[1]+'> </div> <div class="carousel-item"> <img class="d-block w-100" alt="Carousel Bootstrap Third" src='+src[2]+' onclick='+selectf[2]+'> </div> </div> <a class="carousel-control-prev" href="#carousel-19915" data-slide="prev"> <span class="carousel-control-prev-icon"></span> <span class="sr-only">Previous</span> </a> <a class="carousel-control-next" href="#carousel-19915" data-slide="next"> <span class="carousel-control-next-icon"></span> <span class="sr-only">Next</span> </a> </div> </div> </div> </div> </div>';
document.getElementById("bootstrapSelect").innerHTML=bootstrapSelect;
