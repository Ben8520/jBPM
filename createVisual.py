#!/usr/bin/python3

import math
import svgwrite

x_max = 800
y_max = 600

x_svg = x_max/2
y_svg = 0

rect_width = 80
rect_height = 30

# Functions definition

# returns line_number
def find_string(many_lines, str):
    retVal = -1
    line_number = 0
    for line in many_lines:
        retVal = line.find(str)
        if retVal != -1:
            return line_number
        line_number += 1
    return retVal

def print_ten_lines():
    line = 0
    for i in range(0, 10):
        print lines[line]
        line += 1

def get_block_type(line):
    if line.find("<fork") != -1:
       return "fork"
    elif line.find("<state") != -1:
        return "state"
    elif line.find("<decision") != -1:
        return "decision"
    return None

def get_block_name(line):
    start_position = line.find("name=\"")
    end_position = line.find("\">")

    if start_position != -1:
        return line[start_position + 6:end_position]
    

def get_first_block(many_lines, block_name):
        
    block_start = find_string(many_lines, "name=\"" + block_name + "\">")
    if block_start == -1:
        return None

    block_type = get_block_type(many_lines[block_start])

    block_end = find_string(many_lines[block_start:], "</" + block_type + ">")
    if block_end == -1:
        return None

    retVal = (many_lines[block_start:block_start + block_end + 1], block_type)
    
    return retVal
        
def get_directions_of_transition(block):
    retVal = []
    for line in block:
        if line.find("<transition") != -1:
            retVal += [get_transition_direction(line)]
    return retVal

def get_transition_lines(block):
    retVal = []
    for line in block:
        if line.find("<transition") != -1:
            retVal += [line]
    return retVal

def get_legit_directions_of_transition(block):
    retVal = []
    for line in block:
        if line.find("<transition") != -1:
            transition_name = get_transition_direction(line)
            block_name = get_block_name(block[0])
            
            # print "BLOCK " + block_name
            # print "TRANSITION " + transition_name

            if (block_name != None) & (transition_name != None) & (transition_name != "end"):
                if transition_is_legit(transition_name, block_name):
                    retVal += [get_transition_direction(line)]
            else:
                retVal += [get_transition_direction(line)]
    return retVal

def transition_is_legit(transition_name, block_name):
    if get_block_type((get_first_block(lines, block_name)[0])[0]) != "decision":
        return True

    # print
    # print 
    
    for transition_line in get_transition_lines(get_first_block(lines, block_name)[0]):
        # print transition_name
        # print transition_line
        if transition_name in transition_line:
            if ("retour" not in transition_line) & ("directionFaux" not in transition_line):
                # print "TRUE"
                # print 
                return True
    
    next_blocks = get_directions_of_transition(get_first_block(lines, transition_name)[0])
    done_blocks = [transition_name]
    
    while True:
        # print "NEXT BLOCKS"
        # print next_blocks

        if not next_blocks:
            # print "true"
            # print
            return True
        
        if (block_name in next_blocks):
            # print "FALSE"
            # print 
            return False
        else:
            if next_blocks[0] not in done_blocks:
                done_blocks += [next_blocks[0]]
            # print "DONE BLOCKS"
            # print done_blocks
            # print
            
            if next_blocks[0] == 'end':
                del next_blocks[0]
                continue
            
            transitions = get_directions_of_transition(get_first_block(lines,
                                                                  next_blocks[0])[0])
            for transition in transitions:
                if (transition not in next_blocks) & (transition not in done_blocks):
                    next_blocks += [transition]
                
            del next_blocks[0]

        if not next_blocks:
            # print "true"
            # print
            return True

    return True
    

def get_transition_direction(line):
    start_position = line.find("to=\"")
    end_position = line.find("\">")
    if end_position == -1:
        end_position = line.find("\"/>")
    if end_position == -1:
        end_position = line.find("\" />")

    if start_position != -1:
        return line[start_position + 4:end_position]
    

def output_block_tuple(svg_document, block_tuple):
    if block_tuple[1] == "state":
        first_line = block_tuple[0][0]
        output_state(svg_document, get_block_name(first_line))
    else:
        output_state(svg_document, "TODO")
    
def output_state(svg_document, state_name):
    global y_svg
    svg_document.add(svg_document.rect(insert = (x_svg - rect_width/2, y_svg),
                                       size = (str(rect_width) + "px",
                                               str(rect_height) + "px"),
                                       stroke_width = "1",
                                       stroke = "black",
                                       fill = "rgb(255,255,255)"))
    
    svg_document.add(svg_document.text(state_name, insert = (x_svg - rect_width/2 + 40,
                                                             y_svg + 15),
                                       text_anchor = 'middle', fill = 'black',
                                       font_size = '6px'))
    y_svg += 50

def output_start_state(svg_document):
    global y_svg
    rayon = 5
    svg_document.add(svg_document.circle(center = (x_max / 2, y_svg + 2*rayon),
                                         r = rayon,
                                         stroke_width = "1",
                                         stroke = "red",
                                         fill = "rgb(0, 0, 0)"))
    y_svg += 50

def define_transition_tab(many_lines):
    
    start_state_start = find_string(many_lines, "<start-state>")
    start_state_end = find_string(many_lines, "</start-state>")
    start_state_transition = get_directions_of_transition(many_lines[start_state_start:
                                                                start_state_end])

    new_tab = [start_state_transition[0], 1]
    current_tab = [start_state_transition[0], 1]
    blocks_left = [start_state_transition[0]]

    
    for line in many_lines:
        if get_block_type(line) != None:
            current_block = get_first_block(many_lines, get_block_name(line))[0]
            transitions = get_legit_directions_of_transition(current_block)
            for transition in transitions:
                if transition in new_tab:
                    new_tab[new_tab.index(transition) + 1] += 1
                else:
                    new_tab += [transition, 1]
                    current_tab += [transition, 0]
                    blocks_left += [transition]
            
            
    return_tabs = (new_tab, current_tab, blocks_left)
    return return_tabs
        

# End of functions definition 

# Starting main program
inputfile = open('aoo_2016.xml', "r")
svg_document = svgwrite.Drawing(filename = "test-svgwrite.svg",
                                size = (str(x_max) + "px", str(y_max) + "px"))

lines = inputfile.readlines()

tabs = define_transition_tab(lines)

ref_transition_table = tabs[0]
current_transition_table = tabs[1]
blocks_left = tabs[2]

# print ref_transition_table

number_of_block = 0
if len(ref_transition_table) != 2*len(blocks_left):
    print "ERROR !"
else:
    number_of_block = len(blocks_left)

# print number_of_block
    
output_start_state(svg_document)
current_blocks = [blocks_left[0]]
blocks_to_print = []

TIMEOUT = 0

while blocks_left:

    # print "Blocks left: " + str(len(blocks_left))
    # print
    # print "Current block(s): "
    # print current_blocks
    # print
    
    # Avoid infinite loop
    TIMEOUT += 1                
    if TIMEOUT > 60:
        break

    next_blocks = []
    # Update current transition table
    for block in current_blocks:
        whole_block = get_first_block(lines, block)[0]
        transitions = get_directions_of_transition(whole_block)
        # print "Transition from " + block
        # print transitions

        for transition in transitions:
            current_transition_table[current_transition_table.
                                     index(transition) + 1] += 1
            if current_transition_table[current_transition_table.
                                        index(transition) + 1] == ref_transition_table[ref_transition_table.index(transition) + 1]:
                next_blocks += [transition]
            # else:
            #     print "Not in next blocks list yet"
            #     print transition
                

        # print

    # Update current_blocks list
    current_blocks = next_blocks

    # Deplace block from blocks_left list to blocks_to_print list
    for i in range(0,number_of_block):
        if current_transition_table[2*i+1] == ref_transition_table[2*i+1]:
            ref_transition_table[2*i+1] = 0
            block_to_move = current_transition_table[2*i]
            blocks_to_print += [block_to_move]
            del blocks_left[blocks_left.index(block_to_move)]

    # Print the blocks
    for block in blocks_to_print:
        if block != "end":
            block_tuple = get_first_block(lines, block)
            output_block_tuple(svg_document, block_tuple)

    # Empty blocks to print list
    blocks_to_print = []
            
svg_document.save()
inputfile.close()

print ("Done !")
