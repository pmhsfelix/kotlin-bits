package org.pedrofelix.bits.validation

import org.junit.Assert.*
import org.junit.Test
import org.pedrofelix.bits.fp.curry

class ValidationTests {

    // based on https://fsharpforfunandprofit.com/posts/elevated-world-3/#validation

    // a class to represent an address
    data class Address(val addressLines: List<String>)

    // the class that we want to instantiate if validation is successful
    data class Customer(val name: String, val email: String, val address: Address)

    // helper validation functions (just for testing purposes)
    fun validateName(nameString: String): Result<String> =
        if (nameString.isNotEmpty()) Success(nameString)
        else Failure(listOf("invalid name"))

    fun validateEmail(emailString: String): Result<String> =
        if (emailString.contains("@")) Success(emailString)
        else Failure(listOf("invalid email"))

    fun validateAddress(addressLines: List<String>): Result<Address> =
        if (addressLines.all { it.isNotEmpty() }) Success(Address(addressLines))
        else Failure(listOf("address line must not be empty"))

    // the validation function, using the applicative's pattern (see last line)
    fun createValidatedCustomer(
        nameString: String,
        emailString: String,
        addressLines: List<String>
    ): Result<Customer> {
        val name = validateName(nameString)
        val email = validateEmail(emailString)
        val address = validateAddress(addressLines)
        return retn(::Customer.curry()) * name * email * address
    }

    @Test
    fun valid() {
        assertTrue(
            createValidatedCustomer("Alice", "alice@example.com", listOf("Wonderland"))
                    is Success<Customer>
        )
    }

    @Test
    fun invalid_name() {
        when (val res = createValidatedCustomer("", "alice@example.com", listOf("Wonderland"))) {
            is Success<Customer> -> fail("unexpected success")
            is Failure<Customer> -> assertEquals("invalid name", res.errors[0])
        }
    }

    @Test
    fun invalid_name_and_address() {
        when (val res = createValidatedCustomer("", "alice@example.com", listOf(""))) {
            is Success<Customer> -> fail("unexpected success")
            is Failure<Customer> -> {
                assertEquals("invalid name", res.errors[0])
                assertEquals("address line must not be empty", res.errors[1])
            }
        }
    }
}
