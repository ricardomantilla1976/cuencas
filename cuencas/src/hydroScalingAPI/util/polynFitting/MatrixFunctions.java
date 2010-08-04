/***************************************************************************
 *   Copyright (C) 2009 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package hydroScalingAPI.util.polynFitting;

/**
 *
 * @author lutusp
 */
public final class MatrixFunctions {

    PolySolve parent;

    public MatrixFunctions(PolySolve parent) {
        this.parent = parent;
    }

    void gj_divide(double[][] A, int i, int j, int m) {
        for (int q = j + 1; q < m; q++) {
            A[i][q] /= A[i][j];
        }
        A[i][j] = 1;
    }

    void gj_eliminate(double[][] A, int i, int j, int n, int m) {
        for (int k = 0; k < n; k++) {
            if (k != i && A[k][j] != 0) {
                for (int q = j + 1; q < m; q++) {
                    A[k][q] -= A[k][j] * A[i][q];
                }
                A[k][j] = 0;
            }
        }
    }

    void gj_echelonize(double[][] A) {
        int n = A.length;
        int m = A[0].length;
        int i = 0;
        int j = 0;
        int k;
        double temp[];
        while (i < n && j < m) {
            //look for non-zero entries in col j at or below row i
            k = i;
            while (k < n && A[k][j] == 0) {
                k++;
            }
            // if an entry is found at row k
            if (k < n) {
                //  if k is not i, then swap row i with row k
                if (k != i) {
                    temp = A[i];
                    A[i] = A[k];
                    A[k] = temp;
                }
                // if A[i][j] is != 1, divide row i by A[i][j]
                if (A[i][j] != 1) {
                    gj_divide(A, i, j, m);
                }
                // eliminate all other non-zero entries
                gj_eliminate(A, i, j, n, m);
                i++;
            }
            j++;
        }
    }

    public double corr_coeff(Pair[] data, double[] terms) {
        double r = 0;
        int n = data.length;
        double sx = 0, sx2 = 0, sy = 0, sy2 = 0, sxy = 0;
        double x, y;
        for (Pair pr : data) {
            x = parent.fx(pr.x, terms);
            y = pr.y;
            sx += x;
            sy += y;
            sxy += x * y;
            sx2 += x * x;
            sy2 += y * y;
        }
        double div = Math.sqrt((sx2 - (sx * sx) / n) * (sy2 - (sy * sy) / n));
        if (div != 0) {
            r = Math.pow((sxy - (sx * sy) / n) / div, 2);
        }
        return r;
    }

    public double std_error(Pair[] data, double[] terms) {
        double r = 0;
        int n = data.length;
        if (n > 2) {
            double a = 0;
            for (Pair pr : data) {
                a += Math.pow((parent.fx(pr.x, terms) - pr.y), 2);
            }
            r = Math.sqrt(a / (n - 2));
        }
        return r;
    }

    public double[] polyregress(Pair[] data, int p) {
        p += 1;
        int n = data.length;
        int r, c, i, a, b;
        double q;
        int rs = 2 * p - 1;
        //
        // by request: read each datum only once
        // not the most efficient processing method
        // but required if the data set is huge
        //
        // create square matrix with added RH column
        double[][] m = new double[p][p + 1];
        // create array of precalculated matrix data
        double[] mpc = new double[rs];
        mpc[0] = n;
        for (Pair pr : data) {
            // process precalculation array
            for (r = 1; r < rs; r++) {
                mpc[r] += Math.pow(pr.x, r);
            }
            // process RH column cells
            m[0][p] += pr.y;
            for (r = 1; r < p; r++) {
                m[r][p] += Math.pow(pr.x, r) * pr.y;
            }
        }
        // populate square matrix section
        for (r = 0; r < p; r++) {
            for (c = 0; c < p; c++) {
                m[r][c] = mpc[r + c];
            }
        }
        //parent.show_mat(m);
        // reduce matrix
        gj_echelonize(m);
        //parent.show_mat(m);
        // extract rh column
        double[] result = new double[p];
        for (int j = 0; j < p; j++) {
            result[j] = m[j][p];
        }
        return result;
    }
}
