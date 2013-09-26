class Baaar {
	private msg : string;
	
	constructor(msg : string) {
        this.msg = msg;
    }
	
	test() {
		return this.msg;
	}
}

var x = new Baaar("bar");

var y = x.test();

