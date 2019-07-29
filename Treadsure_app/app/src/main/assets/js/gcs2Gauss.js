//X東西
//Y南北
let gcs2Gauss = function (Lon, Lat) {

    const dn = 0.0016792203946287445;
    let X, Y;
    let Lon0=120.97388;
    let Lat0=23.97565;
    Lon = Deg2Rad(Lon);
    Lat = Deg2Rad(Lat);
    Lon0 = Deg2Rad(Lon0);
    Lat0 = Deg2Rad(Lat0);
    
    let dt = Math.sinh(Math.atanh(Math.sin(Lat)) - (2 * Math.sqrt(dn)) / (1 + dn) * Math.atanh(2 * Math.sqrt(dn) / (1 + dn) * Math.sin(Lat)));
    let dtb = Math.sqrt(1 + Math.pow(dt, 2));
    let dLmc = Math.cos(Lon - Lon0);
    let dLms = Math.sin(Lon - Lon0);
    let dXi = Math.atan(dt / dLmc);
    let dEt = Math.atanh(dLms / dtb);
    
    let dal = [];
    dal[0] = 0;
    dal[1] = 0.0008377318247285465;
    dal[2] = 7.608527848379248e-7;
    dal[3] = 1.1976455002315586e-9;
    dal[4] = 2.4291502606542476e-12;
    dal[5] = 5.750164384091974e-15;
    
    let dSg = 0;
    let dTu = 0;
    for (let j = 1; j <= 5; j++) {
        dSg = dSg + 2 * j * dal[j] * Math.cos(2 * j * dXi) * Math.cosh(2 * j * dEt);
        dTu = dTu + 2 * j * dal[j] * Math.sin(2 * j * dXi) * Math.sinh(2 * j * dEt);
    }
    dSg = 1 + dSg;
    
    let dA = [];
    dA[0] = 1.0000007049454078;
    dA[1] = -0.0025188297041239312;
    dA[2] = 0.0000026435429493240994;
    dA[3] = -3.4526259073074147e-9;
    dA[4] = 4.8918304243879506e-12;
    dA[5] = -7.228726045813916e-15;
    
    let dAb = 6377499.1863 / (1 + dn) * dA[0];
    let dSb = 0;
    for (let j = 1; j <= 5; j++) {
        dSb = dSb + dA[j] * Math.sin(2 * j * Lat0);
    }
    dSb = 6377499.1863 / (1 + dn) * (dA[0] * Lat0 + dSb);
    
    Y = 0;
    X = 0;
    for (let j = 1; j <= 5; j++) {
        Y = Y + dal[j] * Math.sin(2 * j * dXi) * Math.cosh(2 * j * dEt);
        X = X + dal[j] * Math.cos(2 * j * dXi) * Math.sinh(2 * j * dEt);
    }
    Y = dAb * (dXi + Y) - dSb;
    X = dAb * (dEt + X);
    return [X, Y];
    };
    
    let Deg2Rad = function (Deg) {
    return (Math.PI * Deg / 180.0);
    }