function generateKey() {
    const rsa = new RSAKey();
    const DEBUG = commonData.print_debug_to_console;
    if (DEBUG) {
        console.log('generating RSA...');
    }
    rsa.generate(commonData.key_length, commonData.global_rsa_e); // 1024 bits, public exponent = 10001


    const public_key = rsa.n.toString(16);
    const private_key = rsa.d.toString(16);

    if (DEBUG) {
        console.log('RSA generated.');
        console.log('public key  =' + public_key);
        console.log('private key =' + private_key);
    }
    $('.private_key').show();
    $('#public_key').text(public_key);
    $('#private_key').text(private_key);
}

function generatePass() {
    var pass_alphabet = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var pass = "";
    var pass_length = 8;
    for (i = 0; i < pass_length; i++) {
        var ch = pass_alphabet.charAt(Math.floor(Math.random() * pass_alphabet.length));
        pass += ch;
    }
    $('#admin_pass').val(pass);
}