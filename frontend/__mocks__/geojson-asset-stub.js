// Stub untuk asset .geojson saat Jest. Di runtime Metro, require('*.geojson')
// mengembalikan module ID asset untuk dipakai expo-asset. Di Jest, kita tidak
// menjalankan loader benar-benar — service akan men-stub `Asset.fromModule`
// di jest-setup.js.
module.exports = 0;
