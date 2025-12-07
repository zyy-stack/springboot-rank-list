import numpy as np


def get_entropy(C):
    _,counts=np.unique(C,return_counts=True)
    probilities=counts/len(C)
    return -np.sum(probilities*np.log2(probilities))

def conditional_entory(D,C,feature_idx):
    feature_values=D[:,feature_idx]
    unique_values,values_counts=np.unique(feature_values,return_counts=True)
    total_samples=len(C)
    cond_entropy=0.0
    for value,count in zip(unique_values,values_counts):
        mask=feature_values==value
        C_subset=C[mask]

        if len(C_subset)>0:
            subset_entropy=get_entropy(C_subset)
            p_value= count/total_samples
            cond_entropy+=p_value*subset_entropy

    return cond_entropy

def information_gain(D,C,feature_idex):
    total_entropy=get_entropy(C)
    cond_entropy=conditional_entory(D,C,feature_idex)
    return total_entropy-cond_entropy

def load_data():
    n=int(input())
    D=[]
    C=[]
    for i in range(n):
        data=list(map(int,input().split()))
        D.append(data[:-1])
        C.append(data[-1])
    D=np.array(D)
    C=np.array(C)
    return D,C

if __name__=="__main__":
    entropy_list=[]
    D,C=load_data()
    for i in range(D.shape[1]):
        gain=information_gain(D,C,i)
        entropy_list.append(gain)
    print(entropy_list)
    print(np.argmax(np.array(entropy_list)))
    entropy_list.sort(reverse=True)
    print(entropy_list[0])

import numpy as np


class PageRank:
    def __init__(self, N, M, I, K, alpha=0.85):
        self.N = N
        self.M = M
        self.I = I
        self.K = K
        self.alpha=alpha
        pass

    def predict(self, data):
        T = data
        M = np.zeros(T.shape)
        L = np.sum(T, axis=1)
        for i in range(T.shape[0]):
            for j in range(T.shape[1]):
                if L[i]!=0:
                    M[j, i] = T[i, j] / L[i]
        V=L
        for _ in range(self.I):
           V =(1-self.alpha) / self.N + self.alpha*np.dot(M,V.T)
        s=V.argsort()[::-1]
        return "\n".join(["%d %.2f" % (i,V[i]) for i in s[:self.K]])

def load_data():
    N, M, I, K = map(int, input().split())
    data = np.zeros((N,N))
    for i in range(M):
        c1,c2,num=map(int, input().split())
        data[c1,c2]+=num
    print(data)
    return N, M, I, K, data


if __name__ == '__main__':
    N, M, I, K, data = load_data()
    pr = PageRank(N, M, I, K)
    print(pr.predict(data))


