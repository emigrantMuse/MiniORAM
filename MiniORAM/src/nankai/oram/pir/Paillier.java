package nankai.oram.pir;
/** 
* This program is free software: you can redistribute it and/or modify it 
* under the terms of the GNU General Public License as published by the Free 
* Software Foundation, either version 3 of the License, or (at your option) 
* any later version. 
* 
* This program is distributed in the hope that it will be useful, but WITHOUT 
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
* FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
* more details. 
* 
* You should have received a copy of the GNU General Public License along with 
* this program. If not, see <http://www.gnu.org/licenses/>. 
*/  
  
import java.math.*;  
import java.util.*;  
  
/** 
 * Paillier Cryptosystem <br> 
 * <br> 
 * References: <br> 
 * [1] Pascal Paillier, 
 * "Public-Key Cryptosystems Based on Composite Degree Residuosity Classes," 
 * EUROCRYPT'99. URL: 
 * <a href="http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf">http: 
 * //www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf</a><br> 
 * 
 * [2] Paillier cryptosystem from Wikipedia. URL: 
 * <a href="http://en.wikipedia.org/wiki/Paillier_cryptosystem">http://en. 
 * wikipedia.org/wiki/Paillier_cryptosystem</a> 
 *  
 * @author Kun Liu (kunliu1@cs.umbc.edu) 
 * @version 1.0 
 */  
public class Paillier {  
  
	/**
	 * For a data with the length bigger than the bitlength, we will divide the data into blocks with length of bitlength-zeroNumbers.
	 */
	public final static int zeroNumbers = 10; 
    /** 
     * p and q are two large primes. lambda = lcm(p-1, q-1) = 
     * (p-1)*(q-1)/gcd(p-1, q-1). 
     */  
    private BigInteger p, q, lambda;  
    /** 
     * n = p*q, where p and q are two large primes. 
     */  
    public BigInteger n;  
    /** 
     * nsquare = n*n 
     */  
    public BigInteger nsquare;  
    /** 
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1. 
     */  
    private BigInteger g;  
    /** 
     * number of bits of modulus 
     */  
    private int bitLength;  
  
    /** 
     * Constructs an instance of the Paillier cryptosystem. 
     *  
     * @param bitLengthVal 
     *            number of bits of modulus 
     * @param certainty 
     *            The probability that the new BigInteger represents a prime 
     *            number will exceed (1 - 2^(-certainty)). The execution time of 
     *            this constructor is proportional to the value of this 
     *            parameter. 
     */  
    public Paillier(int bitLengthVal, int certainty) {  
        KeyGeneration(bitLengthVal, certainty);  
    }  
  
    /** 
     * Constructs an instance of the Paillier cryptosystem with 512 bits of 
     * modulus and at least 1-2^(-64) certainty of primes generation. 
     */  
    public Paillier() {  
        KeyGeneration(1024, 64);  
    }  
  
    /** 
     * Sets up the public key and private key. 
     *  
     * @param bitLengthVal 
     *            number of bits of modulus. 
     * @param certainty 
     *            The probability that the new BigInteger represents a prime 
     *            number will exceed (1 - 2^(-certainty)). The execution time of 
     *            this constructor is proportional to the value of this 
     *            parameter. 
     */  
    public void KeyGeneration(int bitLengthVal, int certainty) {  
        bitLength = bitLengthVal;  
        /* 
         * Constructs two randomly generated positive BigIntegers that are 
         * probably prime, with the specified bitLength and certainty. 
         */  
        p = new BigInteger(bitLength / 2, certainty, new Random());  
        q = new BigInteger(bitLength / 2, certainty, new Random());  
  
        n = p.multiply(q);  
        nsquare = n.multiply(n);  
  
        g = new BigInteger("2");  
        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))  
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));  
        /* check whether g is good. */  
        if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() != 1) {  
            System.out.println("g is not good. Choose g again.");  
            System.exit(1);  
        }  
    }  
  
    /** 
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function 
     * explicitly requires random input r to help with encryption. 
     *  
     * @param m 
     *            plaintext as a BigInteger 
     * @param r 
     *            random plaintext to help with encryption 
     * @return ciphertext as a BigInteger 
     */  
    public BigInteger Encryption(BigInteger m, BigInteger r) {  
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);  
    }  
  
    /** 
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function 
     * automatically generates random input r (to help with encryption). 
     *  
     * @param m 
     *            plaintext as a BigInteger 
     * @return ciphertext as a BigInteger 
     */  
    public BigInteger Encryption(BigInteger m) {  
        BigInteger r = new BigInteger(bitLength, new Random());  
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);  
    }  
  
    /** 
     * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where 
     * u = (L(g^lambda mod n^2))^(-1) mod n. 
     *  
     * @param c 
     *            ciphertext as a BigInteger 
     * @return plaintext as a BigInteger 
     */  
    public BigInteger Decryption(BigInteger c) {  
        BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);  
        return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);  
    }  
  
    /** 
     * sum of (cipher) em1 and em2 
     *  
     * @param em1 
     * @param em2 
     * @return 
     */  
    public BigInteger cipher_add(BigInteger em1, BigInteger em2) {  
        return em1.multiply(em2).mod(nsquare);  
    }  
 
   public int getCiphertextLength()
   {
	   return this.bitLength/4;
   }
   
   /**
    * To make sure the plaintext can be encrypt normally
    * We divide the plaintext into blocks with length of bitlength/8 -1; 
    * @return
    */
   public int getPlaintextLen()
   {
	   //return n.toString(16).length()-1; 
	   return this.bitLength/8 - zeroNumbers;
   }
}  