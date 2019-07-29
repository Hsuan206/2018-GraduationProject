document.getElementById("arCam").innerHTML = '<video id="video" autoplay></video>';
document.getElementById("vrScene").innerHTML = '<a-scene id="scene" vr-mode-ui="enabled: false"> <a-camera id="camera" position="0 0 0"></a-camera> <a-assets id="assets"> <a-asset-item id="bulbasaur" src="gltf/bulbasaur/scene.gltf"></a-asset-item> <a-asset-item id="geodude" src="gltf/geodude/scene.gltf"></a-asset-item> <a-asset-item id="poliwhirl" src="gltf/poliwhirl/scene.gltf"></a-asset-item> </a-assets> </a-scene>';

//video id="video"
//a-scene id="scene"
//a-camera id="camera"
//a-assets id="assets"
let front = false;
let constraints = { video: { facingMode: (front ? "user" : "environment"), width: 800, height: 450 } };
navigator.mediaDevices.getUserMedia(constraints)
    .then(function (mediaStream) {
        var video = document.querySelector('video');
        video.srcObject = mediaStream;
        video.onloadedmetadata = function (e) {
            video.play();
        };
    })
    .catch(function (err) { console.log(err.name + ": " + err.message); });

/////////////////////////////////////////////////////////////////////////////
const sensor = new AbsoluteOrientationSensor();
sensor.start();
sensor.onerror = event => alert(event.error.name, event.error.message);

var deltaAlpha = 0;
sensor.onreading = () => {
    var o = sensor.quaternion;

    var alpha = quaternionToEulerAngleX(o[0], o[1], o[2], o[3]);

    var camera = document.getElementById("camera");

    deltaAlpha = camera.object3D.rotation.y - alpha;
    document.getElementById("deltaAlpha").value = Math.round(deltaAlpha / Math.PI * 180);


};

var quaternionToEulerAngleX = (w, x, y, z) => {
    t0 = 2.0 * (w * x + y * z);
    t1 = 1.0 - 2.0 * (x * x + y * y);
    X = Math.atan2(t0, t1);

    return X;
}

let rotationMatrix = (x, y, theta) => {
    return [Math.cos(theta) * x - Math.sin(theta) * y, Math.sin(theta) * x + Math.cos(theta) * y];
};




var getObjtheta = (x, z) => {
    var d = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)) * (-1);
    var t = (x > 0) ? Math.acos(z / d) * (-1) : (x < 0) ? Math.acos(z / d) : -Math.acos(z / d) * (-1);
    return t;
};
var cursor = function (x, z) {
    var camera = document.getElementById("camera");
    var r_y = camera.object3D.rotation.y;
    var r_x = camera.object3D.rotation.x;

    var c_x = camera.object3D.position.x;
    var c_z = camera.object3D.position.z;
    deltaX = Math.pow((c_x - x), 2);
    deltaZ = Math.pow((c_z - z), 2);

    var xMaxTheta = Math.asin(1 / Math.sqrt(deltaX + deltaZ + 1));
    var yMaxTheta = getObjtheta(x - c_x + 0.5, z - c_z - 0.5);
    var yMinTheta = getObjtheta(x - c_x - 0.5, z - c_z + 0.5);
    [yMaxTheta, yMinTheta] = (yMaxTheta > yMinTheta) ? [yMaxTheta, yMinTheta] : [yMinTheta, yMaxTheta];

    return (r_x <= xMaxTheta && r_x >= 0) && (yMaxTheta >= r_y && yMinTheta <= r_y)
}
//創物件
// let btn = () => {
//     setPosition(3, 4, "fgg", 1);
//     setPosition(-3, 4, "fg", 3);
//     setPosition(3, -4, "f", 2);
//     setPosition(-3, -4, "fgggg", 1);
// };
//刪物件
// let removeObj = () => {
//     var arPosition = rotationMatrix(3, 4, deltaAlpha);
//     var scene = document.getElementById("scene");
//     (cursor(-arPosition[0],arPosition[1]))?scene.removeChild(document.getElementById("fgg")):doNothing();
// }
let doNothing = () => { };
//批次創物件
// var saveDeltaAlpha;
// let arraySetPosition = () =>{
//     saveDeltaAlpha=deltaAlpha;
//     //setPosition(a[0][0],a[0][1],a[0][2],a[0][3]);
// };
//更新物件array
