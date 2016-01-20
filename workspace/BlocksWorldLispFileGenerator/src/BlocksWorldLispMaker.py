'''
Created on Jul 29, 2014

@author: Dhershkowitz
'''
import random

numStartStacks = 3
numGoalStacks = 2
numBlocks = 8
currBlockIndex = 0

unUsedBlocks = []

lastBlockNameString = ""

def refreshUnusedBlocks():
    global unUsedBlocks
    for i in range (0, numBlocks):
        unUsedBlocks.append(i)

def getRandomBlockName():
    global unUsedBlocks
    global lastBlockNameString 
    
    blockNumber = random.choice(unUsedBlocks)
    unUsedBlocks.remove(blockNumber)
    
    toReturn = "block" + str(blockNumber)
    lastBlockNameString = toReturn
    
    return toReturn

"""
CFG::=

CODE ::= (defproblem problem blocks\n(PROBLEMSTART)(GOALS))
PROBLEMSTART ::= BLOCKSLIST STACKSLIST
BLOCKSLIST ::= BLOCK BLOCKSLIST | epsilon
BLOCK ::= (block BLOCKNAME)
BLOCKNAME ::= b<current blockname>

STACKSLIST ::= STACK | epsilon
STACK ::= (on-table BLOCKNAME) STACK | (on BLOCKNAME BLOCKNAME) STACK | (clear BLOCKNAME) | epsilon

GOALS ::= (achieve-goals(STACKSLIST))




"""

def code():
    toReturn = "(defproblem problem blocks\n(" + problemStart() + ")" + "\n(" + goals() + ")\n)"
    
    return toReturn

def problemStart():
    global numStartStacks
    return blocksList() + stacksList(False, numStartStacks)

def blocksList():
    global currBlockIndex
    toReturn = ""
    for i in range(0, numBlocks):
        toReturn += "\t(block " + blockName() + ")\n"
    currBlockIndex = 0
    return toReturn

def stacksList(randomized, stacksToUse):
    if (randomized):
        refreshUnusedBlocks()
    
    global currBlockIndex
    toReturn = ""
    for i in range(0, stacksToUse):
        toReturn += stack(randomized, stacksToUse)
        
    #Add remaining blocks
    if not randomized:
        while (currBlockIndex < numBlocks):
            currBlockName = blockName()
            toReturn += "\t(on-table " + currBlockName + ")" + "(clear " + currBlockName + ")\n"
            
        
        
    currBlockIndex = 0
    return toReturn

def lastBlockName():
    global lastBlockNameString
    return lastBlockNameString

def blockName():
    global currBlockIndex
    global lastBlockNameString
    toReturn = "block" + str(currBlockIndex)
    currBlockIndex += 1
    lastBlockNameString = toReturn
    return toReturn

def stack(randomized, stacksToUse):
    toReturn = "\t"
    numBlocksForThisStack = numBlocks/stacksToUse

    for i in range(0, numBlocksForThisStack+1):
        if i == 0:
            blockNameToUse = getRandomBlockName() if randomized else blockName()
            toReturn += "(on-table " + blockNameToUse + ")"
        elif i == numBlocksForThisStack:
            toReturn += "(clear " + lastBlockName() + ")"
        else:
            lastBlockNameString = lastBlockName()
            toReturn += "(on " + (getRandomBlockName() if randomized else blockName())  + " " 
            toReturn +=  lastBlockNameString +")"
        
    toReturn += "\n"
    return toReturn
        
def goals():
    global numGoalStacks
    global unUsedBlocks
    toReturn = "(achieve-goals\n(" + stacksList(True, numGoalStacks)
    
    while len(unUsedBlocks) > 0:
        currBlockName = getRandomBlockName()
        toReturn += "(on-table " + currBlockName + ")" + "(clear " + currBlockName + ")"
    
    toReturn += ")\n)"
    return toReturn

def main():
    jshopDirectory = "/Users/Dhershkowitz/Desktop/JSHOP2/examples/blocks/"
    fileName = "problem"
    codeString = code()
    
    file = open(jshopDirectory + fileName, "w")
    file.write(codeString)
    file.close()
    
    print "Done writing blocks problem to file!"

if __name__ == '__main__':
    main()
