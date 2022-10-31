import org.checkerframework.checker.dividebyzero.qual.*;

// A simple test case for your divide-by-zero checker.
// The file contains "// ::" comments to indicate expected
// errors and warnings.
//
// Passing this test does not guarantee a perfect grade on this assignment,
// but it is an important start. You should always write your own test cases,
// in addition to using those provided to you.
class Foo {
    public static void refinment1(int y, int x) {
        // Compare 0
        if (y < 0) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        
        if (y <= 0) {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        else {
            int z = 1 / y;
        }

        if (y == 0) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }

        if (y > 0) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y != 0) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
    }

    public static void refinement2(int x, int y) {        
        // Compare to x (top)
        if (y < x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        
        if (y <= x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        
        if (y == x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y >= x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y > x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y != x) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
    }

    public static void refinement3(int x, int y) {
        // Compare to negative value (< 0)
        if (y < -2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        
        if (y <= -2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }
        
        if (y == -2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y >= -2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }

        if (y > -2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }

        if (y != -2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }
    }

    public static void refinement4(int x, int y) {
        // Compare to positive value (> 0)
        if (y < 2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }
        
        if (y <= 2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }
        
        if (y == 2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y >= 2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y > 2) {
            int z = 1 / y;
        } else {
            // :: error: divide.by.zero
            int z = 1 / y;
        }

        if (y != 2) {
            // :: error: divide.by.zero
            int z = 1 / y;
        } else {
            int z = 1 / y;
        }
    }

    public static void refinement5(int x, int y) {
        // Compare when lhs isn't top
        if (y <= 0) {
            // y now <=0
            // Compare 0
            if (y < 0) {
                int z = 1 / y;
            } else {
                // :: error: divide.by.zero
                int z = 1 / y;
            }
            
            if (y >= 0) {
                // :: error: divide.by.zero
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y == 0) {
                // :: error: divide.by.zero
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y >= 0) {
                // :: error: divide.by.zero
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y > 0) {
                int z = 1 / y;
            } else {
                // :: error: divide.by.zero
                int z = 1 / y;
            }

            if (y != 0) {
                int z = 1 / y;
            } else {
                // :: error: divide.by.zero
                int z = 1 / y;
            }
        }
        
        if (y > 0) {
            // y now >0
            // Compare 0
            if (y < 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }
            
            if (y <= 0) {
                int z = 1 / y;
            }
            else {
                int z = 1 / y;
            }

            if (y == 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y >= 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y > 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y != 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }
        }

        if (y != 0) {
            // y now !=0
            // Compare 0
            if (y < 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }
            
            if (y <= 0) {
                int z = 1 / y;
            }
            else {
                int z = 1 / y;
            }

            if (y == 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y >= 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y > 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }

            if (y != 0) {
                int z = 1 / y;
            } else {
                int z = 1 / y;
            }
        }
    }

    public static void f() {
        int one  = 1;
        int zero = 0;
        // :: error: divide.by.zero
        int x    = one / zero;
        int y    = zero / one;
        // :: error: divide.by.zero
        int z    = x / y;
        String s = "hello";
        int foo = (1 / 7) / (3 / 1);
    }

    public static void g(int y) {
        if (y == 0) {
            // :: error: divide.by.zero
            int x = 1 / y / y;
        } else {
            int x = 1 / y;
        }

        if (y != 0) {
            int x = 1 / y;
        } else {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (!(y == 0)) {
            int x = 1 / y;
        } else {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (!(y != 0)) {
            // :: error: divide.by.zero
            int x = 1 / y;
        } else {
            int x = 1 / y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (y <= 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        }

        if (y > 0) {
            int x = 1 / y;
        }

        if (y >= 0) {
            // :: error: divide.by.zero
            int x = 1 / y;
        }
    }

    public static void h() {
        int zero_the_hard_way = 0 + 0 - 0 * 0;
        // :: error: divide.by.zero
        int x = 1 / zero_the_hard_way;

        int one_the_hard_way = 0 * 1 + 1;
        int y = 1 / one_the_hard_way;
    }

    public static void l() {
        // :: error: divide.by.zero
        int a = 1 / (1 - 1);
        int y = 1;
        // :: error: divide.by.zero
        int x = 1 / (y - y);
        int z = y-y;
        // :: error: divide.by.zero
        int k = 1/z;
    }

    public static void mod(int x, int y) {
        // :: error: divide.by.zero
        if (x / y == 1) {
            int z = 23;
        }

        int a = 1 % -1;
        // :: error: divide.by.zero
        int b = 1 % 0;
        int c = 1 % 1;

        int d = -1 % -1;
        // :: error: divide.by.zero
        int e = -1 % 0;
        int f = -1 % 1;
    }

    public static void compoundAssignments(int x, int y) {
        // :: error: divide.by.zero
        x /= 0;

        int k = 4;
        // :: error: divide.by.zero
        k /= (((5 * 0) - 3) + 6 / 2);

        k /= ((5 * 5) + 5 / 2);

        if (y != 0) {
            int j = 1;
            j /= y;
        }

        if (y < 0) {
            int j = 1;
            j /= y;
        }

        if (y <= 0) {
            int j = 1;
            // :: error: divide.by.zero
            j /= y;
        }
    }
}
