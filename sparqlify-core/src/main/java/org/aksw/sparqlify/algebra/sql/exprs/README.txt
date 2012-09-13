I was not sure whether we have to duplicate the jenas expression hierarchy
in order to add a datatype field. For testing purposes I already created
some classes.

But thinking about it, computing the datatype of a compound expression
might be not too different from evaluating its value:

Rather than replacing variables with concrete values, we replace them with concrete datatypes
However, constants have to be treated with special care:

given the expression
a < '1'

and a turns out to be an integer, we are left with the tree
lt(a, '1')

and we can see that '1' can be cast to int, so we get
lt(a, atoi('1'))

which further evaluates to
lt(a, 1)

only now we can can compute the datatype:
lt(int, int)

SqlType evalType(SqlTypeInt, SqlTypeInt) {
	return SqlTypeInt.getInstance();
}

