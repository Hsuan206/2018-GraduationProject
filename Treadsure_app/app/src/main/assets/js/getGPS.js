
let something, options;
function success(pos) {
  var crd = pos.coords;
  document.getElementById("lat").value = crd.latitude;
  document.getElementById("lon").value = crd.longitude;
}
function error(err) {
  console.warn('ERROR(' + err.code + '): ' + err.message);
}
options = {
  enableHighAccuracy: true,
  timeout: 5000,
  maximumAge: 0
};
something = navigator.geolocation.watchPosition(success, error, options);