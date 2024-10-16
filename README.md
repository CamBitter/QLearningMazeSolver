# QLearningMazeSolver
Utilizes Q-Learning neural network to find the most efficient path through a maze.

# To Use

Download as zip, extract the folder contents.
Build the desired maze using the emojis in map.txt

![Screenshot 2024-10-16 at 5 23 07 PM](https://github.com/user-attachments/assets/f34a2759-4361-470c-93e1-033763ab450b)

Run QLearning.java

![Screenshot 2024-10-16 at 5 24 40 PM](https://github.com/user-attachments/assets/f003d241-8dce-4f82-b314-1b3093c12942)

The program will first parse the emoji map into an array of states and will determine valid starting states. 
It then creates the reward matrix which determines the positive or negative value associated with each move from each state.
Then it iterates through the specified number of generations, exploring the maze and updating the Q-Matrix with the appropiate values.
Then it does one final run through the completed Q-Matrix to find the most highly rewarded route, or what it found to be the most efficient route.
This map is then rendered as text in the terminal output.
